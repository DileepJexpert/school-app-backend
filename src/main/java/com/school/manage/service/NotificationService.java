package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Notification;
import com.school.manage.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Creates a new notification. Sets the createdAt timestamp before saving.
     */
    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    /**
     * Returns all notifications in the system (for admin use).
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Returns all active (non-expired) notifications relevant to a student.
     * This includes:
     *  - Notifications targeted to ALL
     *  - Notifications targeted to the student's class
     *  - Notifications targeted directly to this individual student
     */
    public List<Notification> getNotificationsForStudent(String studentId, String className) {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> all = new ArrayList<>();

        // Broadcast to everyone
        all.addAll(notificationRepository.findByTargetAudience("ALL"));

        // Class-specific
        all.addAll(notificationRepository
                .findByTargetAudienceAndTargetClass("CLASS_SPECIFIC", className));

        // Individual
        all.addAll(notificationRepository
                .findByTargetAudienceAndTargetStudentId("INDIVIDUAL", studentId));

        // Filter out expired notifications
        all.removeIf(n -> n.getExpiresAt() != null && n.getExpiresAt().isBefore(now));

        return all;
    }

    /**
     * Returns all notifications for a specific class.
     * Includes ALL + CLASS_SPECIFIC notifications for that class.
     */
    public List<Notification> getNotificationsForClass(String className) {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> all = new ArrayList<>();

        all.addAll(notificationRepository.findByTargetAudience("ALL"));
        all.addAll(notificationRepository
                .findByTargetAudienceAndTargetClass("CLASS_SPECIFIC", className));

        all.removeIf(n -> n.getExpiresAt() != null && n.getExpiresAt().isBefore(now));

        return all;
    }

    /**
     * Returns notifications filtered by type (e.g. FEE_REMINDER, EXAM).
     */
    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    /**
     * Marks a notification as read.
     */
    public Notification markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + id));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Updates an existing notification.
     */
    public Notification updateNotification(String id, Notification updated) {
        Notification existing = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + id));
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        return notificationRepository.save(updated);
    }

    /**
     * Deletes a notification by ID.
     */
    public void deleteNotification(String id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }
}
