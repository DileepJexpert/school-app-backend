package com.school.manage.controller;

import com.school.manage.model.ChatMessage;
import com.school.manage.model.ChatRoom;
import com.school.manage.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatRoom>> getMyRooms(@RequestParam String userId) {
        return ResponseEntity.ok(chatService.getRoomsForUser(userId));
    }

    @PostMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatRoom> getOrCreateRoom(@RequestBody Map<String, Object> body) {
        String userId1 = (String) body.get("userId1");
        String userId2 = (String) body.get("userId2");
        String studentId = (String) body.get("studentId");
        @SuppressWarnings("unchecked")
        Map<String, String> participantNames = (Map<String, String>) body.get("participantNames");
        @SuppressWarnings("unchecked")
        Map<String, String> participantRoles = (Map<String, String>) body.get("participantRoles");
        return ResponseEntity.ok(
                chatService.getOrCreateRoom(userId1, userId2, studentId, participantNames, participantRoles));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(roomId, page, size));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String roomId,
            @RequestBody Map<String, String> body) {
        String senderId = body.get("senderId");
        String senderName = body.get("senderName");
        String senderRole = body.get("senderRole");
        String message = body.get("message");
        return ResponseEntity.ok(
                chatService.sendMessage(roomId, senderId, senderName, senderRole, message));
    }

    @PutMapping("/rooms/{roomId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String roomId,
            @RequestParam String userId) {
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }
}
