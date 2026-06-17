package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "awards")
public class Award {
    @Id
    private String id;
    private String studentId;
    private String studentName;
    private String className;
    private String title;           // "Science Olympiad Winner"
    private String description;
    private String category;        // ACADEMIC, SPORTS, ARTS, SCIENCE, CULTURAL, LEADERSHIP, OTHER
    private String level;           // SCHOOL, DISTRICT, STATE, NATIONAL, INTERNATIONAL
    private String position;        // "1st", "2nd", "3rd", "Participation", "Merit"
    private LocalDate dateAwarded;
    private String awardedBy;       // person/organization
    private String certificateUrl;  // optional link/upload
    private String academicYear;
    private String addedBy;         // admin who added
    private LocalDateTime createdAt;
}
