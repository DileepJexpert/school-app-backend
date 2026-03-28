package com.school.manage.service;

import com.school.manage.dto.AiChatRequest;
import com.school.manage.dto.AiChatResponse;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.*;
import com.school.manage.model.AiConversation.AiMessage;
import com.school.manage.repository.*;
import com.school.manage.service.ai.AiProvider;
import com.school.manage.service.ai.AiProvider.AiProviderResponse;
import com.school.manage.service.ai.AiProvider.ProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiHomeworkService {

    private final AiConfigRepository aiConfigRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiUsageRepository aiUsageRepository;
    private final HomeworkRepository homeworkRepository;
    private final StudentRepository studentRepository;
    private final List<AiProvider> aiProviders;

    private Map<String, AiProvider> providerMap;

    private Map<String, AiProvider> getProviderMap() {
        if (providerMap == null) {
            providerMap = aiProviders.stream()
                    .collect(Collectors.toMap(AiProvider::getName, Function.identity()));
        }
        return providerMap;
    }

    public AiChatResponse chat(AiChatRequest request, User user) {
        String tenantId = user.getTenantId();
        AiConfig config = aiConfigRepository.findByTenantId(tenantId)
                .orElseGet(() -> createDefaultConfig(tenantId));

        if (!config.isEnabled()) {
            throw new IllegalStateException("AI Homework Helper is not enabled for your school");
        }

        String mode = request.getMode() != null ? request.getMode().toUpperCase() : "TUTOR";
        if (!config.getEnabledModes().contains(mode)) {
            throw new IllegalStateException("Mode '" + mode + "' is not enabled. Available: " + config.getEnabledModes());
        }

        // Check daily limit
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        long questionsToday = aiUsageRepository.countByStudentIdAndRequestTimestampAfter(
                user.getId(), startOfDay);
        if (questionsToday >= config.getDailyLimitPerStudent()) {
            throw new IllegalStateException("Daily limit reached (" + config.getDailyLimitPerStudent()
                    + " questions). Try again tomorrow!");
        }

        // Load or create conversation
        AiConversation conversation;
        if (request.getConversationId() != null) {
            conversation = aiConversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        } else {
            conversation = new AiConversation();
            conversation.setStudentId(user.getId());
            conversation.setHomeworkId(request.getHomeworkId());
            conversation.setMode(mode);
            conversation.setCreatedAt(LocalDateTime.now());

            // Set subject/class from homework if linked
            if (request.getHomeworkId() != null) {
                homeworkRepository.findById(request.getHomeworkId()).ifPresent(hw -> {
                    conversation.setSubject(hw.getSubject());
                    conversation.setClassName(hw.getClassName());
                });
            }

            // Add system prompt
            String systemPrompt = buildSystemPrompt(mode, conversation.getSubject(),
                    conversation.getClassName(), request.getLanguage());
            conversation.getMessages().add(new AiMessage("SYSTEM", systemPrompt));
        }

        // Check turn limit
        if (conversation.getMessages().size() >= config.getMaxConversationTurns() * 2) {
            throw new IllegalStateException("Conversation limit reached. Start a new conversation.");
        }

        // Add user message
        conversation.getMessages().add(new AiMessage("USER", request.getMessage()));

        // Call AI provider (primary, then fallback)
        String providerUsed;
        AiProviderResponse aiResponse;
        try {
            providerUsed = config.getPrimaryProvider();
            aiResponse = callProvider(providerUsed, config, conversation.getMessages());
        } catch (Exception e) {
            log.warn("[AiHomeworkService] Primary provider {} failed: {}", config.getPrimaryProvider(), e.getMessage());
            if (config.getFallbackProvider() != null) {
                providerUsed = config.getFallbackProvider();
                aiResponse = callProvider(providerUsed, config, conversation.getMessages());
            } else {
                recordUsage(user, request, config.getPrimaryProvider(), 0, 0, false, e.getMessage());
                throw new RuntimeException("AI provider unavailable. Please try again later.");
            }
        }

        // Add assistant response to conversation
        conversation.getMessages().add(new AiMessage("ASSISTANT", aiResponse.content()));
        conversation.setTotalInputTokens(conversation.getTotalInputTokens() + aiResponse.inputTokens());
        conversation.setTotalOutputTokens(conversation.getTotalOutputTokens() + aiResponse.outputTokens());
        conversation.setLastMessageAt(LocalDateTime.now());
        aiConversationRepository.save(conversation);

        // Record usage
        recordUsage(user, request, providerUsed, aiResponse.inputTokens(),
                aiResponse.outputTokens(), true, null);

        return AiChatResponse.builder()
                .conversationId(conversation.getId())
                .message(aiResponse.content())
                .mode(mode)
                .provider(providerUsed)
                .tokensUsed(AiChatResponse.TokenUsage.builder()
                        .input(aiResponse.inputTokens())
                        .output(aiResponse.outputTokens())
                        .build())
                .dailyUsage(AiChatResponse.DailyUsage.builder()
                        .questionsToday(questionsToday + 1)
                        .limitPerDay(config.getDailyLimitPerStudent())
                        .build())
                .build();
    }

    public List<AiConversation> getConversations(String studentId) {
        return aiConversationRepository.findByStudentIdOrderByLastMessageAtDesc(studentId);
    }

    public AiConversation getConversation(String conversationId) {
        return aiConversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    public List<AiUsageRecord> getUsage(String studentId) {
        return aiUsageRepository.findByStudentIdOrderByRequestTimestampDesc(studentId);
    }

    public List<AiUsageRecord> getUsageReport(LocalDateTime from, LocalDateTime to) {
        return aiUsageRepository.findByRequestTimestampBetween(from, to);
    }

    private AiProviderResponse callProvider(String providerName, AiConfig config,
                                            List<AiMessage> messages) {
        AiProvider provider = getProviderMap().get(providerName);
        if (provider == null) {
            throw new IllegalStateException("Unknown AI provider: " + providerName);
        }

        ProviderConfig providerConfig = switch (providerName) {
            case "OLLAMA" -> new ProviderConfig(null, config.getOllamaBaseUrl(), config.getOllamaModel());
            case "GEMINI" -> new ProviderConfig(config.getGeminiApiKey(), null, config.getGeminiModel());
            case "CLAUDE" -> new ProviderConfig(config.getClaudeApiKey(), null, config.getClaudeModel());
            default -> throw new IllegalStateException("Unknown provider: " + providerName);
        };

        return provider.chat(providerConfig, messages);
    }

    private String buildSystemPrompt(String mode, String subject, String className, String language) {
        String subjectCtx = subject != null ? "Subject: " + subject + ". " : "";
        String classCtx = className != null ? "Student's class: " + className + ". " : "";
        String langCtx = "en".equals(language) ? "" : "Respond in " + language + ". ";

        return switch (mode) {
            case "TUTOR" -> "You are a Socratic tutor for school students. " + subjectCtx + classCtx + langCtx
                    + "NEVER give the direct answer. Instead, guide the student step by step with questions and hints. "
                    + "If the student is stuck, break the problem into smaller parts. "
                    + "Celebrate progress and encourage them. Keep explanations age-appropriate.";
            case "SOLVE" -> "You are a homework helper for school students. " + subjectCtx + classCtx + langCtx
                    + "Provide a clear, step-by-step solution to the student's question. "
                    + "Explain each step so the student understands the reasoning. "
                    + "Use simple language appropriate for their grade level.";
            case "PRACTICE" -> "You are a practice problem generator for school students. " + subjectCtx + classCtx + langCtx
                    + "When the student shares a problem, generate 3 similar practice problems at the same difficulty level. "
                    + "After the student attempts each problem, tell them if they're correct and explain any mistakes. "
                    + "Vary the numbers/details but keep the same concept.";
            default -> "You are a helpful homework assistant for school students. " + subjectCtx + classCtx + langCtx;
        };
    }

    private void recordUsage(User user, AiChatRequest request, String provider,
                             int inputTokens, int outputTokens, boolean success, String error) {
        AiUsageRecord record = new AiUsageRecord();
        record.setStudentId(user.getId());
        record.setStudentName(user.getFullName());
        record.setHomeworkId(request.getHomeworkId());
        record.setMode(request.getMode());
        record.setProvider(provider);
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        record.setEstimatedCostCents(estimateCost(provider, inputTokens, outputTokens));
        record.setRequestTimestamp(LocalDateTime.now());
        record.setSuccess(success);
        record.setErrorMessage(error);
        aiUsageRepository.save(record);
    }

    private double estimateCost(String provider, int inputTokens, int outputTokens) {
        return switch (provider) {
            case "OLLAMA" -> 0.0; // Free local model
            case "GEMINI" -> (inputTokens * 0.000075 + outputTokens * 0.0003); // Gemini Flash pricing
            case "CLAUDE" -> (inputTokens * 0.003 + outputTokens * 0.015); // Claude Sonnet pricing
            default -> 0.0;
        };
    }

    private AiConfig createDefaultConfig(String tenantId) {
        AiConfig config = new AiConfig();
        config.setTenantId(tenantId);
        config.setEnabled(false);
        return aiConfigRepository.save(config);
    }
}
