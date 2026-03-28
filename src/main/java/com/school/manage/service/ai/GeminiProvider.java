package com.school.manage.service.ai;

import com.school.manage.model.AiConversation.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini provider.
 * Uses Gemini 2.0 Flash by default (cheapest: ~$0.10/1M input tokens).
 * API: POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiProvider implements AiProvider {

    private final RestTemplate aiRestTemplate;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    @Override
    public String getName() {
        return "GEMINI";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiProviderResponse chat(ProviderConfig config, List<AiMessage> messages) {
        String model = config.model() != null ? config.model() : "gemini-2.0-flash";
        String apiKey = config.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key not configured");
        }

        String url = BASE_URL + "/" + model + ":generateContent?key=" + apiKey;

        // Separate system instruction from conversation messages
        String systemInstruction = null;
        List<Map<String, Object>> contents = new ArrayList<>();

        for (AiMessage msg : messages) {
            if ("SYSTEM".equalsIgnoreCase(msg.getRole())) {
                systemInstruction = msg.getContent();
                continue;
            }

            Map<String, Object> content = new HashMap<>();
            content.put("role", "USER".equalsIgnoreCase(msg.getRole()) ? "user" : "model");
            content.put("parts", List.of(Map.of("text", msg.getContent())));
            contents.add(content);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("contents", contents);

        if (systemInstruction != null) {
            request.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
        }

        log.info("[GeminiProvider] POST model={} messages={}", model, contents.size());

        Map<String, Object> response = aiRestTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            throw new RuntimeException("Gemini returned null response");
        }

        // Parse: candidates[0].content.parts[0].text
        String content = "";
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> contentObj = (Map<String, Object>) candidate.get("content");
            if (contentObj != null) {
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentObj.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    content = (String) parts.get(0).get("text");
                }
            }
        }

        // Parse: usageMetadata
        int inputTokens = 0, outputTokens = 0;
        Map<String, Object> usage = (Map<String, Object>) response.get("usageMetadata");
        if (usage != null) {
            inputTokens = getIntOrZero(usage, "promptTokenCount");
            outputTokens = getIntOrZero(usage, "candidatesTokenCount");
        }

        log.info("[GeminiProvider] Response: {} chars, tokens in={} out={}", content.length(), inputTokens, outputTokens);
        return new AiProviderResponse(content, inputTokens, outputTokens);
    }

    private int getIntOrZero(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}
