package com.school.manage.service;

import com.school.manage.model.BulkMessage;
import com.school.manage.model.Student;
import com.school.manage.model.User;
import com.school.manage.repository.BulkMessageRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkMessageService {

    private final BulkMessageRepository bulkMessageRepository;
    private final StudentRepository studentRepository;
    private final MongoTemplate mongoTemplate;
    // Note: Don't inject JavaMailSender - just collect recipients and log for now
    // Real email sending would need SMTP config which varies per school

    public BulkMessage sendBulkMessage(BulkMessage message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setSentAt(LocalDateTime.now());
        message.setStatus("SENDING");

        // Collect recipients based on target audience
        List<String> recipients = collectRecipients(message);
        message.setTotalRecipients(recipients.size());

        // For now, simulate sending - log each recipient
        // In production, this would use JavaMailSender for EMAIL
        // and an SMS gateway for SMS
        int sent = 0;
        int failed = 0;
        for (String recipient : recipients) {
            try {
                log.info("[BulkMessage] Would send {} to '{}': subject='{}'",
                        message.getChannel(), recipient, message.getSubject());
                sent++;
            } catch (Exception e) {
                log.error("[BulkMessage] Failed to send to '{}': {}", recipient, e.getMessage());
                failed++;
            }
        }

        message.setSentCount(sent);
        message.setFailedCount(failed);
        message.setStatus("COMPLETED");
        message.setCompletedAt(LocalDateTime.now());

        return bulkMessageRepository.save(message);
    }

    private List<String> collectRecipients(BulkMessage message) {
        List<String> recipients = new ArrayList<>();

        switch (message.getTargetAudience()) {
            case "ALL_PARENTS" -> {
                List<Student> students = studentRepository.findAll().stream()
                        .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                        .toList();
                for (Student s : students) {
                    addParentContacts(s, message.getChannel(), recipients);
                }
            }
            case "CLASS_SPECIFIC" -> {
                List<Student> students = studentRepository.findAll().stream()
                        .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                        .filter(s -> message.getTargetClass() != null &&
                                message.getTargetClass().equals(s.getClassForAdmission()))
                        .toList();
                for (Student s : students) {
                    addParentContacts(s, message.getChannel(), recipients);
                }
            }
            case "ALL_STAFF" -> {
                Query query = new Query(Criteria.where("role").in("TEACHER", "SCHOOL_ADMIN", "ACCOUNTANT", "TRANSPORT_MANAGER"));
                List<User> staffUsers = mongoTemplate.find(query, User.class);
                for (User u : staffUsers) {
                    if ("EMAIL".equals(message.getChannel()) || "BOTH".equals(message.getChannel())) {
                        if (u.getEmail() != null && !u.getEmail().isBlank()) {
                            recipients.add(u.getEmail());
                        }
                    }
                    if ("SMS".equals(message.getChannel()) || "BOTH".equals(message.getChannel())) {
                        if (u.getPhone() != null && !u.getPhone().isBlank()) {
                            recipients.add(u.getPhone());
                        }
                    }
                }
            }
            case "CUSTOM" -> {
                if (message.getCustomRecipients() != null) {
                    recipients.addAll(message.getCustomRecipients());
                }
            }
        }

        // Deduplicate
        return recipients.stream().distinct().collect(Collectors.toList());
    }

    private void addParentContacts(Student s, String channel, List<String> recipients) {
        if (s.getParentDetails() == null) return;
        var pd = s.getParentDetails();
        if ("EMAIL".equals(channel) || "BOTH".equals(channel)) {
            if (pd.getFatherEmail() != null && !pd.getFatherEmail().isBlank()) {
                recipients.add(pd.getFatherEmail());
            }
            if (pd.getMotherEmail() != null && !pd.getMotherEmail().isBlank()) {
                recipients.add(pd.getMotherEmail());
            }
        }
        if ("SMS".equals(channel) || "BOTH".equals(channel)) {
            if (pd.getFatherMobile() != null && !pd.getFatherMobile().isBlank()) {
                recipients.add(pd.getFatherMobile());
            }
            if (pd.getMotherMobile() != null && !pd.getMotherMobile().isBlank()) {
                recipients.add(pd.getMotherMobile());
            }
        }
    }

    public List<BulkMessage> getAllMessages() {
        return bulkMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public BulkMessage getMessageById(String id) {
        return bulkMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));
    }
}
