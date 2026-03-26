package com.school.manage.service;

import com.school.manage.model.ChatMessage;
import com.school.manage.model.ChatRoom;
import com.school.manage.repository.ChatMessageRepository;
import com.school.manage.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatRoom> getRoomsForUser(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsContaining(userId);
        // Enrich with unread counts
        for (ChatRoom room : rooms) {
            long unread = chatMessageRepository.countByRoomIdAndReadFalseAndSenderIdNot(room.getId(), userId);
            if (room.getUnreadCounts() == null) room.setUnreadCounts(new HashMap<>());
            room.getUnreadCounts().put(userId, (int) unread);
        }
        return rooms;
    }

    public ChatRoom getOrCreateRoom(String userId1, String userId2, String studentId,
                                     Map<String, String> names, Map<String, String> roles) {
        // Check if room already exists between these two users for this student
        List<String> participants = List.of(userId1, userId2);
        Optional<ChatRoom> existing = chatRoomRepository
                .findByParticipantsContainingAndStudentId(participants, studentId);

        if (existing.isPresent()) return existing.get();

        ChatRoom room = new ChatRoom();
        room.setParticipants(List.of(userId1, userId2));
        room.setParticipantNames(names);
        room.setParticipantRoles(roles);
        room.setStudentId(studentId);
        room.setUnreadCounts(Map.of(userId1, 0, userId2, 0));
        room.setCreatedAt(LocalDateTime.now());

        if (studentId != null) {
            room.setStudentName(names.getOrDefault("studentName", ""));
        }

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("[ChatService] Chat room created: id='{}', participants={}", saved.getId(), participants);
        return saved;
    }

    public ChatMessage sendMessage(String roomId, String senderId, String senderName,
                                    String senderRole, String message) {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setSenderName(senderName);
        msg.setSenderRole(senderRole);
        msg.setMessage(message);
        msg.setTimestamp(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(msg);

        // Update room's last message
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setLastMessage(message);
            room.setLastMessageAt(saved.getTimestamp());
            chatRoomRepository.save(room);
        });

        return saved;
    }

    public List<ChatMessage> getMessages(String roomId, int page, int size) {
        return chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, PageRequest.of(page, size));
    }

    public void markAsRead(String roomId, String userId) {
        List<ChatMessage> unread = chatMessageRepository
                .findByRoomIdAndReadFalseAndSenderIdNot(roomId, userId);
        unread.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unread);
    }
}
