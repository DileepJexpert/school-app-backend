package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String title;
    private String description;
    private String category; // ACADEMIC, SPORTS, CULTURAL, HOLIDAY, EXAM, PTM, OTHER
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private String targetAudience; // ALL, CLASS_SPECIFIC, STAFF_ONLY
    private String targetClass; // for CLASS_SPECIFIC
    private boolean isHoliday;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active = true;
}
