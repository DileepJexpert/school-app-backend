package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "ai_conversations")
public class AiConversation {

    @Id
    private String id;

    @Indexed
    private String studentId;

    /** Links to Homework.id — null for free-form questions */
    private String homeworkId;

    /** TUTOR | SOLVE | PRACTICE */
    private String mode;

    private String subject;
    private String className;

    private List<AiMessage> messages = new ArrayList<>();

    private boolean sessionActive = true;
    private int totalInputTokens;
    private int totalOutputTokens;

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    @Data
    public static class AiMessage {
        /** SYSTEM | USER | ASSISTANT */
        private String role;
        private String content;
        private LocalDateTime timestamp;

        public AiMessage() {}

        public AiMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
    }
}
