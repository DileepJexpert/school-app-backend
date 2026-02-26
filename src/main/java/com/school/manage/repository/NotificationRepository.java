package com.school.manage.repository;

import com.school.manage.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // All notifications broadcast to everyone
    List<Notification> findByTargetAudience(String targetAudience);

    // Notifications for a specific class
    List<Notification> findByTargetAudienceAndTargetClass(String targetAudience, String targetClass);

    // Notifications for a specific student
    List<Notification> findByTargetAudienceAndTargetStudentId(
            String targetAudience, String targetStudentId);

    // Unread notifications
    List<Notification> findByTargetAudienceAndRead(String targetAudience, boolean read);

    // Notifications not yet expired
    List<Notification> findByExpiresAtAfterOrExpiresAtIsNull(LocalDateTime now);

    // Notifications by type (e.g. FEE_REMINDER, EXAM)
    List<Notification> findByType(String type);
}
