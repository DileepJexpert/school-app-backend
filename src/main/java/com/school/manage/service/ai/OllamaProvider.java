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
 * Ollama provider — FREE local AI for testing.
 * Runs at http://localhost:11434, supports Llama 3, Mistral, Phi-3, etc.
 * No API key needed.
 *
 * Install: curl -fsSL https://ollama.ai/install.sh | sh
 * Pull model: ollama pull llama3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaProvider implements AiProvider {

    private final RestTemplate aiRestTemplate;

    @Override
    public String getName() {
        return "OLLAMA";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiProviderResponse chat(ProviderConfig config, List<AiMessage> messages) {
        String baseUrl = config.baseUrl() != null ? config.baseUrl() : "http://localhost:11434";
        String model = config.model() != null ? config.model() : "llama3";
        String url = baseUrl + "/api/chat";

        // Convert messages to Ollama format
        List<Map<String, String>> ollamaMessages = new ArrayList<>();
        for (AiMessage msg : messages) {
            Map<String, String> m = new HashMap<>();
            m.put("role", mapRole(msg.getRole()));
            m.put("content", msg.getContent());
            ollamaMessages.add(m);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", ollamaMessages);
        request.put("stream", false);

        log.info("[OllamaProvider] POST {} model={} messages={}", url, model, messages.size());

        Map<String, Object> response = aiRestTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            throw new RuntimeException("Ollama returned null response");
        }

        // Parse response
        Map<String, Object> messageObj = (Map<String, Object>) response.get("message");
        String content = messageObj != null ? (String) messageObj.get("content") : "";

        // Ollama returns token counts in eval_count and prompt_eval_count
        int inputTokens = getIntOrZero(response, "prompt_eval_count");
        int outputTokens = getIntOrZero(response, "eval_count");

        log.info("[OllamaProvider] Response: {} chars, tokens in={} out={}", content.length(), inputTokens, outputTokens);
        return new AiProviderResponse(content, inputTokens, outputTokens);
    }

    private String mapRole(String role) {
        return switch (role.toUpperCase()) {
            case "SYSTEM" -> "system";
            case "USER" -> "user";
            case "ASSISTANT" -> "assistant";
            default -> "user";
        };
    }

    private int getIntOrZero(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}
