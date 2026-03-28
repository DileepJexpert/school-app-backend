package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiChatResponse {

    private String conversationId;

    /** The AI's response message */
    private String message;

    /** TUTOR | SOLVE | PRACTICE */
    private String mode;

    /** Which provider handled this request: OLLAMA | GEMINI | CLAUDE */
    private String provider;

    private TokenUsage tokensUsed;
    private DailyUsage dailyUsage;

    @Data
    @Builder
    public static class TokenUsage {
        private int input;
        private int output;
    }

    @Data
    @Builder
    public static class DailyUsage {
        private long questionsToday;
        private int limitPerDay;
    }
}
