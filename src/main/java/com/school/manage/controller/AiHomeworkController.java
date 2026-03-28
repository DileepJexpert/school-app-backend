package com.school.manage.controller;

import com.school.manage.dto.AiChatRequest;
import com.school.manage.dto.AiChatResponse;
import com.school.manage.model.AiConversation;
import com.school.manage.model.AiUsageRecord;
import com.school.manage.model.User;
import com.school.manage.service.AiHomeworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiHomeworkController {

    private final AiHomeworkService aiHomeworkService;

    @PostMapping("/chat")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request,
                                               Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[AiHomeworkController] POST /api/ai/chat — student='{}', mode='{}'",
                user.getFullName(), request.getMode());
        return ResponseEntity.ok(aiHomeworkService.chat(request, user));
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AiConversation>> getConversations(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(aiHomeworkService.getConversations(user.getId()));
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AiConversation> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(aiHomeworkService.getConversation(id));
    }

    @GetMapping("/usage")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AiUsageRecord>> getUsage(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(aiHomeworkService.getUsage(user.getId()));
    }
}
