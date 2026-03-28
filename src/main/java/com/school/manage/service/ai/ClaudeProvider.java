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
            throw new RuntimeException("Claude API key not configured");
        }

        // Separate system prompt from conversation
        String systemPrompt = null;
        List<Map<String, String>> claudeMessages = new ArrayList<>();

        for (AiMessage msg : messages) {
            if ("SYSTEM".equalsIgnoreCase(msg.getRole())) {
                systemPrompt = msg.getContent();
                continue;
            }
            Map<String, String> m = new HashMap<>();
            m.put("role", "USER".equalsIgnoreCase(msg.getRole()) ? "user" : "assistant");
            m.put("content", msg.getContent());
            claudeMessages.add(m);
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

        log.info("[ClaudeProvider] POST model={} messages={}", model, claudeMessages.size());

        Map<String, Object> response = aiRestTemplate.postForObject(API_URL, entity, Map.class);

        if (response == null) {
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

        log.info("[ClaudeProvider] Response: {} chars, tokens in={} out={}", content.length(), inputTokens, outputTokens);
        return new AiProviderResponse(content, inputTokens, outputTokens);
    }

    private int getIntOrZero(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}
