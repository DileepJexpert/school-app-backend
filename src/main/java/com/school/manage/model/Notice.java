package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "notices")
public class Notice {
    @Id
    private String id;
    private String title;
    private String content;
    private String category;         // GENERAL, ACADEMIC, EXAM, FEE, EVENT, HOLIDAY, URGENT
    private String priority;         // LOW, MEDIUM, HIGH, URGENT
    private String targetAudience;   // ALL, STUDENTS, PARENTS, TEACHERS, STAFF, SPECIFIC_CLASS
    private String targetClass;      // if SPECIFIC_CLASS, which class
    private String attachmentUrl;    // optional file/link
    private String attachmentName;
    private boolean pinned;          // pinned notices appear at top
    private boolean published;       // draft vs published
    private String publishedBy;      // admin name
    private String publishedById;    // user ID
    private LocalDate expiryDate;    // auto-hide after this date (optional)
    private List<String> readBy;     // list of user IDs who read it
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
