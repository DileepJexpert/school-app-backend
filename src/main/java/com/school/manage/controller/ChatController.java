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
        @SuppressWarnings("unchecked")
        List<String> participants = (List<String>) body.get("participants");
        String studentId = (String) body.get("studentId");
        @SuppressWarnings("unchecked")
        Map<String, String> participantNames = (Map<String, String>) body.get("participantNames");
        @SuppressWarnings("unchecked")
        Map<String, String> participantRoles = (Map<String, String>) body.get("participantRoles");
        return ResponseEntity.ok(
                chatService.getOrCreateRoom(participants, studentId, participantNames, participantRoles));
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
            @RequestBody ChatMessage message) {
        message.setRoomId(roomId);
        return ResponseEntity.ok(chatService.sendMessage(message));
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
