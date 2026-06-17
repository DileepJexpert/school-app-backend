package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "exam_schedules")
public class ExamSchedule {
    @Id
    private String id;
    private String examName;        // "Mid-Term Exam", "Final Exam", "Unit Test 1"
    private String examType;        // UNIT_TEST, MID_TERM, FINAL, PRACTICAL
    private String academicYear;    // "2025-2026"
    private String className;       // "Class 5", "Class 10"
    private List<ExamSlot> slots;
    private String status;          // DRAFT, PUBLISHED, COMPLETED
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ExamSlot {
        private String subject;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String room;           // "Room 101", "Hall A"
        private String invigilator;    // Teacher name
        private int maxMarks;
    }
}
