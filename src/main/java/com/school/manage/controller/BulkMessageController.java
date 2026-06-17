package com.school.manage.controller;

import com.school.manage.model.BulkMessage;
import com.school.manage.service.BulkMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bulk-messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BulkMessageController {

    private final BulkMessageService bulkMessageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<BulkMessage> sendBulkMessage(@RequestBody BulkMessage message) {
        log.info("[BulkMessageController] POST /api/bulk-messages — channel='{}', audience='{}'",
                message.getChannel(), message.getTargetAudience());
        BulkMessage sent = bulkMessageService.sendBulkMessage(message);
        log.info("[BulkMessageController] Bulk message sent: id='{}', recipients={}, sent={}, failed={}",
                sent.getId(), sent.getTotalRecipients(), sent.getSentCount(), sent.getFailedCount());
        return new ResponseEntity<>(sent, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<BulkMessage>> getAllMessages() {
        log.debug("[BulkMessageController] GET /api/bulk-messages");
        return ResponseEntity.ok(bulkMessageService.getAllMessages());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<BulkMessage> getMessageById(@PathVariable String id) {
        log.debug("[BulkMessageController] GET /api/bulk-messages/{}", id);
        return ResponseEntity.ok(bulkMessageService.getMessageById(id));
    }
}
