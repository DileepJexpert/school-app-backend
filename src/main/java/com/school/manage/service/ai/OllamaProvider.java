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

        log.info("    ┌─── OLLAMA REQUEST ───────────────────────");
        log.info("    │ URL   : {}", url);
        log.info("    │ Model : {}", model);
        log.info("    │ Messages being sent to Ollama:");

        // Convert messages to Ollama format
        List<Map<String, String>> ollamaMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            AiMessage msg = messages.get(i);
            Map<String, String> m = new HashMap<>();
            String role = mapRole(msg.getRole());
            m.put("role", role);
            m.put("content", msg.getContent());
            ollamaMessages.add(m);

            // Log each message (truncated)
            String preview = msg.getContent().length() > 100
                    ? msg.getContent().substring(0, 100) + "..."
                    : msg.getContent();
            log.info("    │   [{}] {} → \"{}\"", i, role.toUpperCase(), preview);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", ollamaMessages);
        request.put("stream", false);

        log.info("    │ Calling Ollama API (stream=false)...");
        long startTime = System.currentTimeMillis();

        Map<String, Object> response;
        try {
            response = aiRestTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("    │ OLLAMA ERROR after {}ms: {}", elapsed, e.getMessage());
            log.error("    │ Is Ollama running? Check: curl {}/api/tags", baseUrl);
            log.error("    │ Is model '{}' pulled? Run: ollama pull {}", model, model);
            log.error("    └───────────────────────────────────────");
            throw e;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        if (response == null) {
            log.error("    │ OLLAMA returned NULL response after {}ms", elapsed);
            log.error("    └───────────────────────────────────────");
            throw new RuntimeException("Ollama returned null response");
        }

        // Parse response
        Map<String, Object> messageObj = (Map<String, Object>) response.get("message");
        String content = messageObj != null ? (String) messageObj.get("content") : "";

        // Ollama returns token counts in eval_count and prompt_eval_count
        int inputTokens = getIntOrZero(response, "prompt_eval_count");
        int outputTokens = getIntOrZero(response, "eval_count");

        // Log detailed response
        log.info("    │");
        log.info("    │ ✓ OLLAMA RESPONSE");
        log.info("    │ Time         : {}ms ({:.1f}s)", elapsed, elapsed / 1000.0);
        log.info("    │ Input tokens : {} (prompt)", inputTokens);
        log.info("    │ Output tokens: {} (generated)", outputTokens);
        log.info("    │ Total tokens : {}", inputTokens + outputTokens);
        log.info("    │ Response len : {} chars", content.length());
        log.info("    │ Cost         : $0.00 (FREE - local model)");

        // Log the actual response (first 200 chars)
        if (content.length() > 200) {
            log.info("    │ AI says: \"{}...\"", content.substring(0, 200));
        } else {
            log.info("    │ AI says: \"{}\"", content);
        }

        // Log model info if available
        if (response.containsKey("model")) {
            log.info("    │ Model used: {}", response.get("model"));
        }
        if (response.containsKey("total_duration")) {
            Object dur = response.get("total_duration");
            if (dur instanceof Number) {
                long nanos = ((Number) dur).longValue();
                log.info("    │ Ollama total_duration: {}ms (includes model load)", nanos / 1_000_000);
            }
        }
        if (response.containsKey("load_duration")) {
            Object dur = response.get("load_duration");
            if (dur instanceof Number) {
                long nanos = ((Number) dur).longValue();
                log.info("    │ Ollama load_duration: {}ms (model loading time)", nanos / 1_000_000);
            }
        }
        if (response.containsKey("eval_duration")) {
            Object dur = response.get("eval_duration");
            if (dur instanceof Number) {
                long nanos = ((Number) dur).longValue();
                double tokensPerSec = outputTokens > 0 ? (outputTokens / (nanos / 1_000_000_000.0)) : 0;
                log.info("    │ Ollama eval_duration: {}ms ({} tokens/sec)", nanos / 1_000_000, String.format("%.1f", tokensPerSec));
            }
        }

        log.info("    └───────────────────────────────────────");
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
