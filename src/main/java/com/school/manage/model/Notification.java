package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String title;
    private String message;

    // GENERAL, FEE_REMINDER, EXAM, EVENT, HOLIDAY, EMERGENCY
    private String type;

    // ALL, CLASS_SPECIFIC, INDIVIDUAL
    private String targetAudience;

    // Populated when targetAudience = CLASS_SPECIFIC
    private String targetClass;

    // Populated when targetAudience = INDIVIDUAL
    private String targetStudentId;

    // LOW, MEDIUM, HIGH
    private String priority = "MEDIUM";

    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;

    private boolean read = false;
}
