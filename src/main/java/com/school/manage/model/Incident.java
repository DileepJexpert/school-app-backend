package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "incidents")
public class Incident {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String className;
    private String academicYear;

    // WARNING, MINOR, MAJOR, CRITICAL
    private String severity;

    // BEHAVIORAL, ACADEMIC, ATTENDANCE, BULLYING, PROPERTY_DAMAGE, OTHER
    private String category;

    private String description;
    private String actionTaken;
    private String reportedBy;
    private LocalDate incidentDate;

    private boolean parentNotified = false;
    private LocalDateTime parentNotifiedAt;

    private String followUpNotes;
    private boolean resolved = false;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}
