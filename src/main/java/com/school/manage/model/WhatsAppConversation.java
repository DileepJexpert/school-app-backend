package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "whatsapp_conversations")
public class WhatsAppConversation {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String phoneNumber;

    private String studentId;
    private String studentName;
    private String parentName;
    private String className;

    private List<WaMessage> messages = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private int messageCountToday;
    private LocalDateTime lastCountReset;

    @Data
    public static class WaMessage {
        private String role;      // USER | ASSISTANT
        private String content;
        private LocalDateTime timestamp;

        public WaMessage() {}

        public WaMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
    }
}
