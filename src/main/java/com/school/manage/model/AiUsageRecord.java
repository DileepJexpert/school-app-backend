package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ai_usage")
public class AiUsageRecord {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentName;
    private String homeworkId;

    /** TUTOR | SOLVE | PRACTICE */
    private String mode;

    /** OLLAMA | GEMINI | CLAUDE */
    private String provider;

    private int inputTokens;
    private int outputTokens;
    private double estimatedCostCents;

    @Indexed
    private LocalDateTime requestTimestamp;

    private boolean success;
    private String errorMessage;
}
