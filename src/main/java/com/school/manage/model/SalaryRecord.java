package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "salary_records")
public class SalaryRecord {

    @Id
    private String id;

    private String staffId;
    private String staffName;
    private String department;
    private String designation;

    private int month; // 1-12
    private int year;

    private double basicPay;
    private double hra;         // House Rent Allowance
    private double da;          // Dearness Allowance
    private double ta;          // Travel Allowance
    private double otherAllowances;

    private double pf;          // Provident Fund deduction
    private double tax;         // TDS
    private double otherDeductions;

    private double grossSalary;
    private double totalDeductions;
    private double netSalary;

    // GENERATED, PAID, HELD
    private String status = "GENERATED";

    private String paymentMode; // BANK_TRANSFER, CHEQUE, CASH
    private String transactionRef;

    private LocalDateTime generatedAt = LocalDateTime.now();
    private LocalDateTime paidAt;
    private String generatedBy;
}
