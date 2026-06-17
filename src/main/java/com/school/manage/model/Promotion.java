package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "promotions")
public class Promotion {

    @Id
    private String id;
    private String studentId;
    private String studentName;
    private String fromClass;         // current class
    private String fromSection;
    private String toClass;           // promoted class
    private String toSection;
    private String fromAcademicYear;  // "2024-25"
    private String toAcademicYear;    // "2025-26"
    private String status;            // PROMOTED, RETAINED, TRANSFERRED, TC_ISSUED
    private String remarks;
    private double percentage;        // overall percentage (optional)
    private String promotedBy;        // admin name
    private LocalDateTime promotedAt;
    private LocalDateTime createdAt;
}
