package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "payroll")
public class Payroll {
    @Id
    private String id;
    private String userId;
    private String staffName;
    private String department;
    private String designation;
    private String month;           // "June"
    private int year;               // 2025
    private double basicSalary;
    private double hra;             // house rent allowance
    private double da;              // dearness allowance
    private double ta;              // transport allowance
    private double otherAllowances;
    private double grossSalary;     // computed
    private double pf;              // provident fund deduction
    private double tax;             // income tax deduction
    private double otherDeductions;
    private double netSalary;       // computed
    private int workingDays;
    private int presentDays;
    private int leaveDays;
    private String status;          // DRAFT, PROCESSED, PAID
    private String paymentMode;     // BANK_TRANSFER, CASH, CHEQUE
    private String bankAccountNumber;
    private LocalDate paymentDate;
    private String processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
