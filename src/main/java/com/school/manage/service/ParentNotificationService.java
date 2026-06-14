package com.school.manage.service;

import com.school.manage.model.*;
import com.school.manage.repository.StudentFeeProfileRepository;
import com.school.manage.repository.StudentRepository;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ParentNotificationService {

    private final WhatsAppService whatsAppService;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final StudentFeeProfileRepository feeProfileRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate platformMongoTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public ParentNotificationService(WhatsAppService whatsAppService,
                                     NotificationService notificationService,
                                     StudentRepository studentRepository,
                                     StudentFeeProfileRepository feeProfileRepository,
                                     MongoTemplate mongoTemplate,
                                     @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.whatsAppService = whatsAppService;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.feeProfileRepository = feeProfileRepository;
        this.mongoTemplate = mongoTemplate;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    @Async
    public void sendAbsenceAlerts(List<Attendance> records, String tenantId) {
        if (tenantId == null) {
            log.warn("[ParentNotification] No tenant context — skipping absence alerts");
            return;
        }
        try {
            TenantContext.setTenant(tenantId);

            WhatsAppConfig config = whatsAppService.getWhatsAppConfig(tenantId);
            if (config == null || !config.isEnabled() || !config.isAbsenceAlertEnabled()) {
                log.debug("[ParentNotification] Absence alerts disabled for tenant '{}'", tenantId);
                return;
            }

            List<Attendance> absentRecords = records.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .toList();

            if (absentRecords.isEmpty()) return;

            log.info("[ParentNotification] Sending {} absence alert(s) for tenant '{}'",
                    absentRecords.size(), tenantId);

            for (Attendance record : absentRecords) {
                try {
                    Student student = studentRepository.findById(record.getStudentId()).orElse(null);
                    if (student == null || student.getParentDetails() == null) continue;

                    String dateStr = record.getDate().format(DATE_FMT);
                    String message = String.format(
                            "Dear Parent,\n\n" +
                            "This is to inform you that your child *%s* (%s) was marked *absent* on %s.\n\n" +
                            "If your child is unwell, please inform the school. " +
                            "For any queries, contact the school office.\n\n" +
                            "— %s School Management",
                            student.getFullName(), record.getClassName(), dateStr, tenantId);

                    List<String> phones = getParentPhones(student.getParentDetails());
                    for (String phone : phones) {
                        whatsAppService.sendWhatsAppReply(formatPhone(phone), config, message);
                    }

                    Notification notification = new Notification();
                    notification.setTitle("Absence Alert: " + student.getFullName());
                    notification.setMessage(student.getFullName() + " was absent on " + dateStr);
                    notification.setType("ATTENDANCE_ALERT");
                    notification.setTargetAudience("INDIVIDUAL");
                    notification.setTargetStudentId(record.getStudentId());
                    notification.setPriority("HIGH");
                    notificationService.createNotification(notification);

                } catch (Exception e) {
                    log.error("[ParentNotification] Failed to send absence alert for student '{}': {}",
                            record.getStudentId(), e.getMessage());
                }
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyFeeReminders() {
        log.info("[ParentNotification] Running daily fee reminder job...");

        List<School> schools = platformMongoTemplate.find(
                Query.query(Criteria.where("active").is(true)), School.class);

        for (School school : schools) {
            try {
                TenantContext.setTenant(school.getTenantId());
                processFeeDuesForTenant(school.getTenantId());
            } catch (Exception e) {
                log.error("[ParentNotification] Fee reminder failed for tenant '{}': {}",
                        school.getTenantId(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        log.info("[ParentNotification] Fee reminder job complete for {} tenant(s)", schools.size());
    }

    public void sendFeeRemindersForTenant(String tenantId) {
        try {
            TenantContext.setTenant(tenantId);
            processFeeDuesForTenant(tenantId);
        } finally {
            TenantContext.clear();
        }
    }

    private void processFeeDuesForTenant(String tenantId) {
        WhatsAppConfig config = whatsAppService.getWhatsAppConfig(tenantId);
        if (config == null || !config.isEnabled() || !config.isFeeReminderEnabled()) {
            return;
        }

        LocalDate targetDate = LocalDate.now().plusDays(config.getFeeReminderDaysBefore());
        List<StudentFeeProfile> allProfiles = feeProfileRepository.findAll();

        int remindersSent = 0;

        for (StudentFeeProfile profile : allProfiles) {
            if (profile.getFeeInstallments() == null) continue;

            List<FeeInstallment> dueInstallments = profile.getFeeInstallments().stream()
                    .filter(i -> "PENDING".equals(i.getStatus()) || "PARTIALLY_PAID".equals(i.getStatus()))
                    .filter(i -> i.getDueDate() != null)
                    .filter(i -> !i.getDueDate().isAfter(targetDate))
                    .toList();

            if (dueInstallments.isEmpty()) continue;

            Student student = studentRepository.findById(profile.getId()).orElse(null);
            if (student == null || student.getParentDetails() == null) continue;
            if (!"ACTIVE".equals(student.getStatus())) continue;

            BigDecimal totalDue = dueInstallments.stream()
                    .map(i -> i.getAmountDue().subtract(i.getAmountPaid()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            StringBuilder feeDetails = new StringBuilder();
            for (FeeInstallment inst : dueInstallments) {
                BigDecimal remaining = inst.getAmountDue().subtract(inst.getAmountPaid());
                feeDetails.append("  • ").append(inst.getInstallmentName())
                        .append(": Rs ").append(remaining);
                if (inst.getDueDate().isBefore(LocalDate.now())) {
                    feeDetails.append(" (overdue)");
                } else {
                    feeDetails.append(" (due ").append(inst.getDueDate().format(DATE_FMT)).append(")");
                }
                feeDetails.append("\n");
            }

            String message = String.format(
                    "Dear Parent,\n\n" +
                    "This is a gentle reminder about pending fees for *%s* (%s).\n\n" +
                    "Pending installments:\n%s\n" +
                    "Total due: *Rs %s*\n\n" +
                    "Please pay at the school office or via online payment.\n\n" +
                    "— %s School Management",
                    student.getFullName(), student.getClassForAdmission(),
                    feeDetails, totalDue, tenantId);

            List<String> phones = getParentPhones(student.getParentDetails());
            for (String phone : phones) {
                whatsAppService.sendWhatsAppReply(formatPhone(phone), config, message);
            }

            Notification notification = new Notification();
            notification.setTitle("Fee Reminder: " + student.getFullName());
            notification.setMessage("Rs " + totalDue + " pending for " + student.getFullName());
            notification.setType("FEE_REMINDER");
            notification.setTargetAudience("INDIVIDUAL");
            notification.setTargetStudentId(student.getId());
            notification.setPriority("MEDIUM");
            notificationService.createNotification(notification);

            remindersSent++;
        }

        if (remindersSent > 0) {
            log.info("[ParentNotification] Sent {} fee reminder(s) for tenant '{}'",
                    remindersSent, tenantId);
        }
    }

    private List<String> getParentPhones(ParentDetails pd) {
        List<String> phones = new ArrayList<>();
        if (pd.getFatherMobile() != null && !pd.getFatherMobile().isBlank()) {
            phones.add(pd.getFatherMobile());
        }
        if (pd.getMotherMobile() != null && !pd.getMotherMobile().isBlank()) {
            if (!phones.contains(pd.getMotherMobile())) {
                phones.add(pd.getMotherMobile());
            }
        }
        return phones;
    }

    private String formatPhone(String phone) {
        String cleaned = phone.replaceAll("[\\s\\-+]", "");
        if (cleaned.length() == 10) {
            return "91" + cleaned;
        }
        return cleaned;
    }
}
