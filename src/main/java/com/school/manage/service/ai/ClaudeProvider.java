package com.school.manage.service.ai;

import com.school.manage.model.AiConversation.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude provider.
 * Uses Claude Sonnet by default (best quality for SOLVE/PRACTICE modes).
 * API: POST https://api.anthropic.com/v1/messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeProvider implements AiProvider {

    private final RestTemplate aiRestTemplate;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    @Override
    public String getName() {
        return "CLAUDE";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiProviderResponse chat(ProviderConfig config, List<AiMessage> messages) {
        String model = config.model() != null ? config.model() : "claude-sonnet-4-20250514";
        String apiKey = config.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.error("    [CLAUDE] API key is NOT SET — cannot call Claude");
            throw new RuntimeException("Claude API key not configured");
        }

        log.info("    ┌─── CLAUDE REQUEST ──────────────────────");
        log.info("    │ Model  : {}", model);
        log.info("    │ API Key: ***{}", apiKey.substring(Math.max(0, apiKey.length() - 4)));

        // Separate system prompt from conversation
        String systemPrompt = null;
        List<Map<String, String>> claudeMessages = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            AiMessage msg = messages.get(i);
            if ("SYSTEM".equalsIgnoreCase(msg.getRole())) {
                systemPrompt = msg.getContent();
                log.info("    │ System prompt: {}chars", msg.getContent().length());
                continue;
            }
            String role = "USER".equalsIgnoreCase(msg.getRole()) ? "user" : "assistant";
            Map<String, String> m = new HashMap<>();
            m.put("role", role);
            m.put("content", msg.getContent());
            claudeMessages.add(m);

            String preview = msg.getContent().length() > 80
                    ? msg.getContent().substring(0, 80) + "..."
                    : msg.getContent();
            log.info("    │   [{}] {} → \"{}\"", i, role, preview);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("max_tokens", 2048);
        request.put("messages", claudeMessages);
        if (systemPrompt != null) {
            request.put("system", systemPrompt);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.info("    │ Calling Claude API (max_tokens=2048)...");
        long startTime = System.currentTimeMillis();

        Map<String, Object> response;
        try {
            response = aiRestTemplate.postForObject(API_URL, entity, Map.class);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("    │ CLAUDE ERROR after {}ms: {}", elapsed, e.getMessage());
            log.error("    └───────────────────────────────────────");
            throw e;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        if (response == null) {
            log.error("    │ CLAUDE returned NULL response after {}ms", elapsed);
            log.error("    └───────────────────────────────────────");
            throw new RuntimeException("Claude returned null response");
        }

        // Parse: content[0].text
        String content = "";
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");
        if (contentList != null && !contentList.isEmpty()) {
            content = (String) contentList.get(0).get("text");
        }

        // Parse: usage.input_tokens, usage.output_tokens
        int inputTokens = 0, outputTokens = 0;
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            inputTokens = getIntOrZero(usage, "input_tokens");
            outputTokens = getIntOrZero(usage, "output_tokens");
        }

        double cost = (inputTokens * 0.003 + outputTokens * 0.015);

        log.info("    │");
        log.info("    │ ✓ CLAUDE RESPONSE");
        log.info("    │ Time         : {}ms", elapsed);
        log.info("    │ Input tokens : {}", inputTokens);
        log.info("    │ Output tokens: {}", outputTokens);
        log.info("    │ Response len : {} chars", content.length());
        log.info("    │ Est. cost    : ${}", String.format("%.6f", cost));
        if (content.length() > 200) {
            log.info("    │ AI says: \"{}...\"", content.substring(0, 200));
        } else {
            log.info("    │ AI says: \"{}\"", content);
        }
        log.info("    └───────────────────────────────────────");

        return new AiProviderResponse(content, inputTokens, outputTokens);
    }

    private int getIntOrZero(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}
