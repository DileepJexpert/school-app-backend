package com.school.manage.controller;

import com.school.manage.model.Notification;
import com.school.manage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification) {
        return new ResponseEntity<>(
                notificationService.createNotification(notification), HttpStatus.CREATED);
    }

    /**
     * Get all notifications (admin view).
     *
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /**
     * Get all notifications relevant to a student (ALL + their class + individual).
     *
     * GET /api/notifications/student/{studentId}?className=Class-5A
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Notification>> getNotificationsForStudent(
            @PathVariable String studentId,
            @RequestParam String className) {
        return ResponseEntity.ok(
                notificationService.getNotificationsForStudent(studentId, className));
    }

    /**
     * Get all notifications for a specific class.
     *
     * GET /api/notifications/class/{className}
     */
    @GetMapping("/class/{className}")
    public ResponseEntity<List<Notification>> getNotificationsForClass(
            @PathVariable String className) {
        return ResponseEntity.ok(notificationService.getNotificationsForClass(className));
    }

    /**
     * Get notifications filtered by type (GENERAL, FEE_REMINDER, EXAM, EVENT, HOLIDAY, EMERGENCY).
     *
     * GET /api/notifications/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @PathVariable String type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    /**
     * Mark a notification as read.
     *
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Update an existing notification.
     *
     * PUT /api/notifications/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable String id,
            @RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.updateNotification(id, notification));
    }

    /**
     * Delete a notification by ID.
     *
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
