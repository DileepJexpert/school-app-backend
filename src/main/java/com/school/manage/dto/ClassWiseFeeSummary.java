package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ClassWiseFeeSummary {

    private String classForAdmission;                  // which class e.g. "Class 1"
    private BigDecimal totalCollectedInClass;          // sum of all amountPaid
    private BigDecimal totalDiscountInClass;           // sum of all discounts
    private int transactionCountInClass;               // how many transactions for this class

    private List<PaymentModeBreakdown> paymentModeBreakdown; // breakdown by paymentMode
}
