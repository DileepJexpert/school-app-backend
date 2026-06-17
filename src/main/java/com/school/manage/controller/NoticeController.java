package com.school.manage.controller;

import com.school.manage.model.Notice;
import com.school.manage.model.User;
import com.school.manage.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Notice>> getAll() {
        log.info("[NoticeController] GET /api/notices");
        return ResponseEntity.ok(noticeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Notice> getById(@PathVariable String id) {
        log.info("[NoticeController] GET /api/notices/{}", id);
        return ResponseEntity.ok(noticeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Notice> create(@RequestBody Notice notice,
                                         Authentication auth) {
        User user = (User) auth.getPrincipal();
        notice.setPublishedBy(user.getFullName());
        notice.setPublishedById(user.getId());
        log.info("[NoticeController] POST /api/notices — by='{}', title='{}'",
                user.getFullName(), notice.getTitle());
        return new ResponseEntity<>(noticeService.create(notice), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Notice> update(@PathVariable String id,
                                         @RequestBody Notice notice) {
        log.info("[NoticeController] PUT /api/notices/{}", id);
        return ResponseEntity.ok(noticeService.update(id, notice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[NoticeController] DELETE /api/notices/{}", id);
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Notice> publish(@PathVariable String id) {
        log.info("[NoticeController] PUT /api/notices/{}/publish", id);
        return ResponseEntity.ok(noticeService.publish(id));
    }

    @PutMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Notice> togglePin(@PathVariable String id) {
        log.info("[NoticeController] PUT /api/notices/{}/pin", id);
        return ResponseEntity.ok(noticeService.togglePin(id));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Notice> markAsRead(@PathVariable String id,
                                             Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[NoticeController] PUT /api/notices/{}/read — userId='{}'", id, user.getId());
        return ResponseEntity.ok(noticeService.markAsRead(id, user.getId()));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notice>> getByCategory(@PathVariable String category) {
        log.info("[NoticeController] GET /api/notices/category/{}", category);
        return ResponseEntity.ok(noticeService.getByCategory(category));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[NoticeController] GET /api/notices/stats");
        return ResponseEntity.ok(noticeService.getStats());
    }
}
