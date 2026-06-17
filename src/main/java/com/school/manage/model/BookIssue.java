package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "book_issues")
public class BookIssue {
    @Id
    private String id;
    private String bookId;
    private String bookTitle; // denormalized for easy display
    private String studentId;
    private String studentName; // denormalized
    private String className;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null if not returned
    private String status; // ISSUED, RETURNED, OVERDUE
    private double fineAmount; // calculated on return if overdue
    private String issuedBy; // staff who issued
    private String remarks;
    private LocalDateTime createdAt;
}
