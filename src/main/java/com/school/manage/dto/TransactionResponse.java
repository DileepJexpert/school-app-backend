package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String studentId;
    private String studentName;
    private String receiptNumber;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;
    private BigDecimal discount;
    private String paymentMode;
    private List<String> paidForMonths;
    private String remarks;
    private String className;
    private String rollNumber;
    private String collectedBy;
}
