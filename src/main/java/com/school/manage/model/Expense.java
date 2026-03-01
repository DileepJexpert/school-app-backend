package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;

    private String title;
    private String category;
    private double amount;

    /** ISO-8601 date — e.g. 2024-06-15 */
    private LocalDate date;

    private String paidTo;
    private String remarks;

    private LocalDateTime createdAt = LocalDateTime.now();
}
