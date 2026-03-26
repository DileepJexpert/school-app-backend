package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String id;

    /** User IDs of participants (typically 2: a teacher and a parent) */
    private List<String> participants;

    /** Display names of participants keyed by userId */
    private Map<String, String> participantNames;

    /** Roles of participants keyed by userId */
    private Map<String, String> participantRoles;

    /** Student this chat is about (provides context for parent-teacher communication) */
    private String studentId;
    private String studentName;

    private String lastMessage;
    private LocalDateTime lastMessageAt;

    /** Unread message count per participant, keyed by userId */
    private Map<String, Integer> unreadCounts;

    private LocalDateTime createdAt = LocalDateTime.now();
}
