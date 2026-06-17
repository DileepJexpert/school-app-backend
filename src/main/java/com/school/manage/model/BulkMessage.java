package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "bulk_messages")
public class BulkMessage {
    @Id
    private String id;
    private String subject;
    private String body;           // message content (plain text or HTML for email)
    private String channel;        // EMAIL, SMS, BOTH
    private String targetAudience; // ALL_PARENTS, CLASS_SPECIFIC, ALL_STAFF, CUSTOM
    private String targetClass;    // for CLASS_SPECIFIC
    private List<String> customRecipients; // list of email/phone for CUSTOM
    private int totalRecipients;
    private int sentCount;
    private int failedCount;
    private String status;         // PENDING, SENDING, COMPLETED, FAILED
    private String sentBy;
    private LocalDateTime sentAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
