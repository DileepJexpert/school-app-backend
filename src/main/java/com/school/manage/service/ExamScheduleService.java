package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.ExamSchedule;
import com.school.manage.model.Notification;
import com.school.manage.repository.ExamScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final NotificationService notificationService;

    public ExamSchedule createSchedule(ExamSchedule schedule) {
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setStatus("DRAFT");
        return examScheduleRepository.save(schedule);
    }

    public ExamSchedule updateSchedule(String id, ExamSchedule schedule) {
        ExamSchedule existing = examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found with id: " + id));
        schedule.setId(id);
        schedule.setCreatedAt(existing.getCreatedAt());
        schedule.setCreatedBy(existing.getCreatedBy());
        schedule.setUpdatedAt(LocalDateTime.now());
        return examScheduleRepository.save(schedule);
    }

    public void deleteSchedule(String id) {
        if (!examScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam schedule not found with id: " + id);
        }
        examScheduleRepository.deleteById(id);
    }

    public ExamSchedule getScheduleById(String id) {
        return examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found with id: " + id));
    }

    public List<ExamSchedule> getAllSchedules(String academicYear) {
        return examScheduleRepository.findByAcademicYearOrderByCreatedAtDesc(academicYear);
    }

    public List<ExamSchedule> getSchedulesByClass(String className, String year) {
        return examScheduleRepository.findByClassNameAndAcademicYear(className, year);
    }

    public List<ExamSchedule> getPublishedSchedules(String className, String year) {
        return examScheduleRepository.findByClassNameAndAcademicYearAndStatus(className, year, "PUBLISHED");
    }

    public ExamSchedule publishSchedule(String id) {
        ExamSchedule schedule = examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found with id: " + id));
        schedule.setStatus("PUBLISHED");
        schedule.setUpdatedAt(LocalDateTime.now());
        ExamSchedule savedSchedule = examScheduleRepository.save(schedule);

        Notification notification = new Notification();
        notification.setTitle("Exam Schedule Published: " + schedule.getExamName());
        notification.setMessage("The exam schedule for " + schedule.getExamName() + " (" + schedule.getClassName() + ") has been published. Please check the exam schedule for details.");
        notification.setType("EXAM");
        notification.setTargetAudience("CLASS_SPECIFIC");
        notification.setTargetClass(schedule.getClassName());
        notification.setPriority("HIGH");
        notification.setCreatedBy(schedule.getCreatedBy());
        notificationService.createNotification(notification);

        return savedSchedule;
    }
}
