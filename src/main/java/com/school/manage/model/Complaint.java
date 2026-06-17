package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "complaints")
public class Complaint {

    @Id
    private String id;
    private String complaintNumber;    // auto: CMP-001, CMP-002
    private String title;
    private String description;
    private String category;           // ACADEMIC, FACILITY, TRANSPORT, FEE, DISCIPLINE, STAFF, OTHER
    private String priority;           // LOW, MEDIUM, HIGH, URGENT
    private String status;             // OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED

    // Who filed it
    private String filedBy;            // user ID
    private String filerName;
    private String filerRole;          // PARENT, STUDENT, TEACHER, STAFF
    private String filerEmail;
    private String filerPhone;

    // Related student (if applicable)
    private String studentId;
    private String studentName;
    private String className;

    // Resolution
    private String assignedTo;         // admin user ID
    private String assignedToName;
    private String resolution;         // resolution notes
    private LocalDateTime resolvedAt;

    // Comments / updates
    private List<ComplaintComment> comments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ComplaintComment {
        private String id;       // UUID
        private String userId;
        private String userName;
        private String userRole;
        private String message;
        private LocalDateTime createdAt;
    }
}
