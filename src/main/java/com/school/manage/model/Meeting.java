package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Document(collection = "meetings")
public class Meeting {

    @Id
    private String id;
    private String title;
    private String description;
    private String meetingType;       // PARENT_TEACHER, STAFF, GENERAL
    private String teacherId;         // user ID of teacher
    private String teacherName;
    private String parentId;          // user ID of parent (for PT meetings)
    private String parentName;
    private String studentId;         // related student
    private String studentName;
    private String className;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;      // default 30
    private String venue;             // room number or "Online"
    private String status;            // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    private String notes;             // post-meeting notes
    private String feedback;          // parent feedback
    private String createdBy;         // who scheduled
    private String createdByRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
