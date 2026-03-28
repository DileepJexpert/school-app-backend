package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "ai_config")
public class AiConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private boolean enabled = false;

    /** Which AI modes are active: TUTOR, SOLVE, PRACTICE */
    private List<String> enabledModes = List.of("TUTOR");

    /** Primary AI provider: OLLAMA, GEMINI, CLAUDE */
    private String primaryProvider = "OLLAMA";

    /** Fallback provider if primary fails */
    private String fallbackProvider = "GEMINI";

    // -- Provider API keys (encrypted in production) --
    private String geminiApiKey;
    private String claudeApiKey;

    // -- Ollama settings (local, free for testing) --
    private String ollamaBaseUrl = "http://localhost:11434";
    private String ollamaModel = "llama3";

    // -- Gemini/Claude model overrides --
    private String geminiModel = "gemini-2.0-flash";
    private String claudeModel = "claude-sonnet-4-20250514";

    // -- Limits --
    private int dailyLimitPerStudent = 20;
    private int maxConversationTurns = 30;
    private Long monthlyBudgetCents;

    // -- Restrictions --
    private List<String> allowedSubjects;  // null = all subjects
    private List<String> allowedGrades;    // null = all grades

    private String updatedBy;
    private LocalDateTime updatedAt;
}
