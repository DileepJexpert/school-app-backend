package com.school.manage.controller;

import com.school.manage.model.Notification;
import com.school.manage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        log.info("[NotificationController] POST /api/notifications — type='{}', audience='{}'",
                notification.getType(), notification.getTargetAudience());
        Notification saved = notificationService.createNotification(notification);
        log.info("[NotificationController] Notification created: id='{}'", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        log.debug("[NotificationController] GET /api/notifications");
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Notification>> getNotificationsForStudent(
            @PathVariable String studentId,
            @RequestParam String className) {
        log.debug("[NotificationController] GET /api/notifications/student/{} class='{}'", studentId, className);
        return ResponseEntity.ok(notificationService.getNotificationsForStudent(studentId, className));
    }

    @GetMapping("/class/{className}")
    public ResponseEntity<List<Notification>> getNotificationsForClass(@PathVariable String className) {
        log.debug("[NotificationController] GET /api/notifications/class/{}", className);
        return ResponseEntity.ok(notificationService.getNotificationsForClass(className));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable String type) {
        log.debug("[NotificationController] GET /api/notifications/type/{}", type);
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        log.info("[NotificationController] PUT /api/notifications/{}/read", id);
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable String id,
            @RequestBody Notification notification) {
        log.info("[NotificationController] PUT /api/notifications/{}", id);
        return ResponseEntity.ok(notificationService.updateNotification(id, notification));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        log.info("[NotificationController] DELETE /api/notifications/{}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
