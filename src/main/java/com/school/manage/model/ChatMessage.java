package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    private String roomId;
    private String senderId;
    private String senderName;
    private String senderRole; // TEACHER, PARENT, SCHOOL_ADMIN

    private String message;

    // TEXT, IMAGE, FILE
    private String messageType = "TEXT";
    private String attachmentUrl;

    private boolean read = false;
    private LocalDateTime timestamp = LocalDateTime.now();
}
