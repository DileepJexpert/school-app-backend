package com.school.manage.service.ai;

import com.school.manage.model.AiConversation.AiMessage;

import java.util.List;

/**
 * Abstraction for AI providers (Gemini, Claude, Ollama).
 * Each provider translates messages to its own API format.
 */
public interface AiProvider {

    /** Provider name: OLLAMA, GEMINI, CLAUDE */
    String getName();

    /**
     * Send messages to the AI provider and get a response.
     *
     * @param config  provider-specific config (API key, base URL, model)
     * @param messages conversation history including system prompt
     * @return AI response with text and token counts
     */
    AiProviderResponse chat(ProviderConfig config, List<AiMessage> messages);

    record ProviderConfig(String apiKey, String baseUrl, String model) {}

    record AiProviderResponse(String content, int inputTokens, int outputTokens) {}
}
