package com.school.manage.service;

import com.school.manage.model.*;
import com.school.manage.model.AiConversation.AiMessage;
import com.school.manage.model.WhatsAppConversation.WaMessage;
import com.school.manage.repository.*;
import com.school.manage.service.ai.AiProvider;
import com.school.manage.service.ai.AiProvider.AiProviderResponse;
import com.school.manage.service.ai.AiProvider.ProviderConfig;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WhatsAppService {

    private final MongoTemplate mongoTemplate;
    private final MongoTemplate platformMongoTemplate;
    private final StudentRepository studentRepository;
    private final HomeworkRepository homeworkRepository;
    private final AttendanceService attendanceService;
    private final FeeService feeService;
    private final ResultService resultService;
    private final AiConfigRepository aiConfigRepository;
    private final WhatsAppConversationRepository waConversationRepository;
    private final List<AiProvider> aiProviders;
    private final RestTemplate restTemplate = new RestTemplate();

    /** Cache: phone number -> {tenantId, studentId} */
    private final ConcurrentHashMap<String, ParentMapping> parentCache = new ConcurrentHashMap<>();

    private Map<String, AiProvider> providerMap;

    private static final String WHATSAPP_API_BASE = "https://graph.facebook.com/v21.0";

    public WhatsAppService(MongoTemplate mongoTemplate,
                           @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate,
                           StudentRepository studentRepository,
                           HomeworkRepository homeworkRepository,
                           AttendanceService attendanceService,
                           FeeService feeService,
                           ResultService resultService,
                           AiConfigRepository aiConfigRepository,
                           WhatsAppConversationRepository waConversationRepository,
                           List<AiProvider> aiProviders) {
        this.mongoTemplate = mongoTemplate;
        this.platformMongoTemplate = platformMongoTemplate;
        this.studentRepository = studentRepository;
        this.homeworkRepository = homeworkRepository;
        this.attendanceService = attendanceService;
        this.feeService = feeService;
        this.resultService = resultService;
        this.aiConfigRepository = aiConfigRepository;
        this.waConversationRepository = waConversationRepository;
        this.aiProviders = aiProviders;
    }

    // ── Main entry point — called from webhook ─────────────────────────────

    /**
     * Process an incoming WhatsApp message.
     * 1. Identify parent by phone number
     * 2. Set tenant context
     * 3. Build school data context
     * 4. Call AI for a natural response
     * 5. Send reply via WhatsApp Cloud API
     */
    public void handleIncomingMessage(String from, String messageText, String messageId) {
        log.info("┌─── WHATSAPP MESSAGE RECEIVED ───────────────────────");
        log.info("│ From     : {}", from);
        log.info("│ Message  : {} ({}chars)", messageText.substring(0, Math.min(80, messageText.length())),
                messageText.length());

        try {
            // 1. Find parent's student
            ParentMapping mapping = identifyParent(from);
            if (mapping == null) {
                log.warn("│ UNKNOWN parent — phone not registered in any school");
                sendWhatsAppReply(from, null,
                        "Sorry, your phone number is not registered with any school on our platform. " +
                        "Please contact your school administration to register.");
                log.info("└─────────────────────────────────────────────────────");
                return;
            }

            log.info("│ Parent   : {} ({})", mapping.parentName, mapping.relation);
            log.info("│ Student  : {} (Class {})", mapping.studentName, mapping.className);
            log.info("│ Tenant   : {}", mapping.tenantId);

            // 2. Set tenant context
            TenantContext.setTenant(mapping.tenantId);

            // 3. Load WhatsApp config
            WhatsAppConfig waConfig = getWhatsAppConfig(mapping.tenantId);
            if (waConfig == null || !waConfig.isEnabled()) {
                log.warn("│ WhatsApp agent DISABLED for tenant '{}'", mapping.tenantId);
                sendWhatsAppReply(from, waConfig,
                        "The WhatsApp assistant is currently not active for your school. Please contact the school directly.");
                return;
            }

            // 4. Check daily limit
            WhatsAppConversation conversation = getOrCreateConversation(from, mapping);
            resetDailyCountIfNeeded(conversation);
            if (conversation.getMessageCountToday() >= waConfig.getDailyLimitPerParent()) {
                log.warn("│ Daily limit reached: {}/{}", conversation.getMessageCountToday(), waConfig.getDailyLimitPerParent());
                sendWhatsAppReply(from, waConfig,
                        "You've reached the daily message limit (" + waConfig.getDailyLimitPerParent() +
                        " messages). Please try again tomorrow or contact the school directly.");
                return;
            }

            // 5. Build school data context
            String schoolContext = buildSchoolContext(mapping);
            log.info("│ Context  : {} chars of school data", schoolContext.length());

            // 6. Build AI messages
            List<AiMessage> messages = buildAiMessages(waConfig, mapping, schoolContext, conversation, messageText);

            // 7. Call AI provider
            log.info("│ Calling AI provider...");
            AiConfig aiConfig = aiConfigRepository.findByTenantId(mapping.tenantId).orElse(null);
            String aiReply = callAi(waConfig, aiConfig, messages);
            log.info("│ AI Reply : {} ({}chars)", aiReply.substring(0, Math.min(100, aiReply.length())),
                    aiReply.length());

            // 8. Save conversation
            conversation.getMessages().add(new WaMessage("USER", messageText));
            conversation.getMessages().add(new WaMessage("ASSISTANT", aiReply));
            conversation.setMessageCountToday(conversation.getMessageCountToday() + 1);
            conversation.setLastMessageAt(LocalDateTime.now());

            // Keep only last 20 messages in conversation for context window
            if (conversation.getMessages().size() > 20) {
                conversation.setMessages(
                        conversation.getMessages().subList(
                                conversation.getMessages().size() - 20,
                                conversation.getMessages().size()));
            }
            waConversationRepository.save(conversation);

            // 9. Send reply via WhatsApp
            sendWhatsAppReply(from, waConfig, aiReply);
            log.info("│ Reply sent successfully");
            log.info("└─────────────────────────────────────────────────────");

        } catch (Exception e) {
            log.error("│ ERROR processing message: {}", e.getMessage(), e);
            log.info("└─────────────────────────────────────────────────────");
        } finally {
            TenantContext.clear();
        }
    }

    // ── Parent Identification ──────────────────────────────────────────────

    /**
     * Find which student belongs to this phone number by searching across all tenants.
     * Results are cached after first lookup.
     */
    ParentMapping identifyParent(String phoneNumber) {
        String normalized = normalizePhone(phoneNumber);

        // Check cache
        if (parentCache.containsKey(normalized)) {
            log.info("│ Parent found in cache");
            return parentCache.get(normalized);
        }

        log.info("│ Searching all tenants for phone: {}", normalized);

        // Get all active schools from platform_db
        List<School> schools = platformMongoTemplate.find(
                Query.query(Criteria.where("active").is(true)), School.class);

        for (School school : schools) {
            try {
                TenantContext.setTenant(school.getTenantId());

                // Search in students collection for parent phone match
                Query query = new Query(new Criteria().orOperator(
                        Criteria.where("parentDetails.fatherMobile").regex(normalized + "$"),
                        Criteria.where("parentDetails.motherMobile").regex(normalized + "$"),
                        Criteria.where("contactDetails.primaryContactNumber").regex(normalized + "$")
                ));
                query.addCriteria(Criteria.where("status").is("ACTIVE"));

                Student student = mongoTemplate.findOne(query, Student.class);

                if (student != null) {
                    String parentName = "Parent";
                    String relation = "parent";

                    if (student.getParentDetails() != null) {
                        if (student.getParentDetails().getFatherMobile() != null &&
                            student.getParentDetails().getFatherMobile().contains(normalized.substring(Math.max(0, normalized.length() - 10)))) {
                            parentName = student.getParentDetails().getFatherName() != null ?
                                    student.getParentDetails().getFatherName() : "Father";
                            relation = "father";
                        } else if (student.getParentDetails().getMotherMobile() != null &&
                                   student.getParentDetails().getMotherMobile().contains(normalized.substring(Math.max(0, normalized.length() - 10)))) {
                            parentName = student.getParentDetails().getMotherName() != null ?
                                    student.getParentDetails().getMotherName() : "Mother";
                            relation = "mother";
                        }
                    }

                    ParentMapping mapping = new ParentMapping(
                            school.getTenantId(),
                            student.getId(),
                            student.getFullName(),
                            student.getClassForAdmission(),
                            parentName,
                            relation,
                            school.getName()
                    );

                    parentCache.put(normalized, mapping);
                    log.info("│ Found student {} in tenant '{}' — matched as {}",
                            student.getFullName(), school.getTenantId(), relation);
                    return mapping;
                }
            } finally {
                TenantContext.clear();
            }
        }

        log.warn("│ Phone {} not found in any tenant", normalized);
        return null;
    }

    // ── Build School Context ───────────────────────────────────────────────

    /**
     * Pull attendance, fees, homework, and results data for the student
     * and format as a text block for the AI system prompt.
     */
    String buildSchoolContext(ParentMapping mapping) {
        StringBuilder ctx = new StringBuilder();

        // Attendance
        try {
            var summary = attendanceService.getAttendanceSummary(
                    mapping.studentId, LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1));
            if (summary != null) {
                ctx.append("ATTENDANCE:\n");
                ctx.append("  Total days: ").append(summary.getTotalDays()).append("\n");
                ctx.append("  Present: ").append(summary.getPresentDays()).append("\n");
                ctx.append("  Absent: ").append(summary.getAbsentDays()).append("\n");
                ctx.append("  Percentage: ").append(String.format("%.1f%%", summary.getAttendancePercentage())).append("\n\n");
            }
        } catch (Exception e) {
            log.debug("│ Could not load attendance: {}", e.getMessage());
        }

        // Fees
        try {
            var feeProfile = feeService.getStudentFeeProfile(mapping.studentId);
            if (feeProfile != null) {
                ctx.append("FEES:\n");
                ctx.append("  Total fee: Rs ").append(feeProfile.getTotalFees()).append("\n");
                ctx.append("  Paid: Rs ").append(feeProfile.getPaidFees()).append("\n");
                ctx.append("  Pending: Rs ").append(feeProfile.getDueFees()).append("\n");
                if (feeProfile.getFeeInstallments() != null) {
                    for (var inst : feeProfile.getFeeInstallments()) {
                        ctx.append("  - ").append(inst.getInstallmentName()).append(": Rs ").append(inst.getAmountDue())
                           .append(" (").append(inst.getStatus()).append(")\n");
                    }
                }
                ctx.append("\n");
            }
        } catch (Exception e) {
            log.debug("│ Could not load fees: {}", e.getMessage());
        }

        // Homework
        try {
            var homeworkList = homeworkRepository.findAll().stream()
                    .filter(h -> mapping.className.equals(h.getClassName()))
                    .filter(h -> "ACTIVE".equals(h.getStatus()) || h.getDueDate() == null || !h.getDueDate().isBefore(LocalDate.now()))
                    .limit(5)
                    .toList();
            if (!homeworkList.isEmpty()) {
                ctx.append("ACTIVE HOMEWORK:\n");
                for (var hw : homeworkList) {
                    ctx.append("  - ").append(hw.getSubject()).append(": ").append(hw.getTitle());
                    if (hw.getDueDate() != null) ctx.append(" (Due: ").append(hw.getDueDate()).append(")");
                    ctx.append("\n");
                }
                ctx.append("\n");
            }
        } catch (Exception e) {
            log.debug("│ Could not load homework: {}", e.getMessage());
        }

        // Recent Results
        try {
            var results = resultService.getResultsForStudent(mapping.studentId);
            if (results != null && !results.isEmpty()) {
                ctx.append("RECENT RESULTS:\n");
                results.stream().limit(5).forEach(r -> {
                    ctx.append("  - ").append(r.getExamType()).append(" | ").append(r.getSubject())
                       .append(": ").append(r.getMarksObtained()).append("/").append(r.getMaxMarks());
                    if (r.getGrade() != null) ctx.append(" (Grade: ").append(r.getGrade()).append(")");
                    ctx.append("\n");
                });
                ctx.append("\n");
            }
        } catch (Exception e) {
            log.debug("│ Could not load results: {}", e.getMessage());
        }

        if (ctx.isEmpty()) {
            ctx.append("No data available yet for this student.\n");
        }

        return ctx.toString();
    }

    // ── AI Call ─────────────────────────────────────────────────────────────

    private List<AiMessage> buildAiMessages(WhatsAppConfig waConfig, ParentMapping mapping,
                                             String schoolContext, WhatsAppConversation conversation,
                                             String userMessage) {
        String systemPrompt = String.format(
                "You are a friendly school assistant for %s. " +
                "You are chatting with %s (%s of %s, %s). " +
                "Answer questions about the child's school life using the data below. " +
                "Be polite, concise (under 300 words), and helpful. " +
                "If the parent asks something not in the data, say you don't have that information and suggest contacting the school. " +
                "Reply in the SAME LANGUAGE the parent uses (Hindi, English, or mixed). " +
                "Use simple formatting — no markdown (WhatsApp doesn't render it well). " +
                "Use line breaks and emojis sparingly for readability.\n\n" +
                "=== STUDENT DATA ===\n%s",
                mapping.schoolName, mapping.parentName, mapping.relation,
                mapping.studentName, mapping.className, schoolContext
        );

        List<AiMessage> messages = new ArrayList<>();
        messages.add(new AiMessage("SYSTEM", systemPrompt));

        // Add recent conversation history (last 6 messages for context)
        if (conversation.getMessages() != null) {
            int start = Math.max(0, conversation.getMessages().size() - 6);
            for (int i = start; i < conversation.getMessages().size(); i++) {
                WaMessage msg = conversation.getMessages().get(i);
                messages.add(new AiMessage(msg.getRole(), msg.getContent()));
            }
        }

        // Add current message
        messages.add(new AiMessage("USER", userMessage));
        return messages;
    }

    private String callAi(WhatsAppConfig waConfig, AiConfig aiConfig, List<AiMessage> messages) {
        if (aiConfig == null) {
            return "I'm sorry, the AI assistant is not configured for your school yet. Please contact the school administration.";
        }

        // Use WhatsApp-specific provider if set, otherwise fall back to school's AI config
        String providerName = waConfig.getAiProvider() != null ? waConfig.getAiProvider() : aiConfig.getPrimaryProvider();

        Map<String, AiProvider> providers = getProviderMap();
        AiProvider provider = providers.get(providerName);
        if (provider == null) {
            log.error("│ Unknown AI provider: {}", providerName);
            return "Sorry, I'm having technical difficulties. Please try again later.";
        }

        ProviderConfig config = switch (providerName) {
            case "OLLAMA" -> new ProviderConfig(null, aiConfig.getOllamaBaseUrl(), aiConfig.getOllamaModel());
            case "GEMINI" -> new ProviderConfig(aiConfig.getGeminiApiKey(), null, aiConfig.getGeminiModel());
            case "CLAUDE" -> new ProviderConfig(aiConfig.getClaudeApiKey(), null, aiConfig.getClaudeModel());
            default -> throw new IllegalStateException("Unknown provider: " + providerName);
        };

        try {
            long start = System.currentTimeMillis();
            AiProviderResponse response = provider.chat(config, messages);
            log.info("│ AI call completed in {}ms — tokens: in={} out={}",
                    System.currentTimeMillis() - start, response.inputTokens(), response.outputTokens());
            return response.content();
        } catch (Exception e) {
            log.error("│ AI call failed: {}", e.getMessage());

            // Try fallback
            if (aiConfig.getFallbackProvider() != null) {
                String fallback = aiConfig.getFallbackProvider();
                AiProvider fallbackProvider = providers.get(fallback);
                if (fallbackProvider != null) {
                    try {
                        log.info("│ Trying fallback provider: {}", fallback);
                        ProviderConfig fallbackConfig = switch (fallback) {
                            case "OLLAMA" -> new ProviderConfig(null, aiConfig.getOllamaBaseUrl(), aiConfig.getOllamaModel());
                            case "GEMINI" -> new ProviderConfig(aiConfig.getGeminiApiKey(), null, aiConfig.getGeminiModel());
                            case "CLAUDE" -> new ProviderConfig(aiConfig.getClaudeApiKey(), null, aiConfig.getClaudeModel());
                            default -> throw new IllegalStateException("Unknown fallback: " + fallback);
                        };
                        AiProviderResponse response = fallbackProvider.chat(fallbackConfig, messages);
                        return response.content();
                    } catch (Exception e2) {
                        log.error("│ Fallback also failed: {}", e2.getMessage());
                    }
                }
            }

            return "I'm sorry, I'm having trouble connecting right now. Please try again in a few minutes or contact the school directly.";
        }
    }

    // ── Send WhatsApp Reply ────────────────────────────────────────────────

    void sendWhatsAppReply(String to, WhatsAppConfig config, String message) {
        if (config == null || config.getWhatsappBusinessToken() == null || config.getWhatsappPhoneNumberId() == null) {
            log.warn("│ Cannot send WhatsApp reply — config/token/phoneNumberId missing. Message: {}", message);
            return;
        }

        String url = WHATSAPP_API_BASE + "/" + config.getWhatsappPhoneNumberId() + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getWhatsappBusinessToken());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            log.info("│ WhatsApp API response: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("│ Failed to send WhatsApp message: {}", e.getMessage());
        }
    }

    // ── Webhook signature verification ─────────────────────────────────────

    public boolean verifySignature(String signature, String payload, String appSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            String expected = "sha256=" + bytesToHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ── Conversation Management ────────────────────────────────────────────

    private WhatsAppConversation getOrCreateConversation(String phone, ParentMapping mapping) {
        return waConversationRepository.findByPhoneNumber(normalizePhone(phone))
                .orElseGet(() -> {
                    WhatsAppConversation conv = new WhatsAppConversation();
                    conv.setPhoneNumber(normalizePhone(phone));
                    conv.setTenantId(mapping.tenantId);
                    conv.setStudentId(mapping.studentId);
                    conv.setStudentName(mapping.studentName);
                    conv.setParentName(mapping.parentName);
                    conv.setClassName(mapping.className);
                    conv.setCreatedAt(LocalDateTime.now());
                    conv.setLastMessageAt(LocalDateTime.now());
                    conv.setMessageCountToday(0);
                    conv.setLastCountReset(LocalDateTime.now());
                    return conv;
                });
    }

    private void resetDailyCountIfNeeded(WhatsAppConversation conversation) {
        if (conversation.getLastCountReset() == null ||
            conversation.getLastCountReset().toLocalDate().isBefore(LocalDate.now())) {
            conversation.setMessageCountToday(0);
            conversation.setLastCountReset(LocalDateTime.now());
        }
    }

    public WhatsAppConfig getWhatsAppConfig(String tenantId) {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(tenantId)), WhatsAppConfig.class);
    }

    public List<WhatsAppConversation> getConversations(String tenantId) {
        return waConversationRepository.findByTenantIdOrderByLastMessageAtDesc(tenantId);
    }

    // ── Admin operations ───────────────────────────────────────────────────

    public WhatsAppConfig saveConfig(WhatsAppConfig config) {
        config.setUpdatedAt(LocalDateTime.now());
        return mongoTemplate.save(config);
    }

    /** Invalidate parent cache — called when student data changes */
    public void clearParentCache() {
        parentCache.clear();
        log.info("WhatsApp parent cache cleared");
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, AiProvider> getProviderMap() {
        if (providerMap == null) {
            providerMap = aiProviders.stream()
                    .collect(Collectors.toMap(AiProvider::getName, Function.identity()));
        }
        return providerMap;
    }

    private String normalizePhone(String phone) {
        // Remove spaces, dashes, +, leading zeros
        String cleaned = phone.replaceAll("[\\s\\-+]", "");
        // Keep last 10 digits for India numbers
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }
        return cleaned;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ── Inner record for parent mapping ────────────────────────────────────

    record ParentMapping(
            String tenantId,
            String studentId,
            String studentName,
            String className,
            String parentName,
            String relation,
            String schoolName
    ) {}
}
