package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SchoolSummaryResponse {
    private int totalStudents;
    private Map<String, Long> enrollmentByClass;   // className -> count
    private BigDecimal totalFeesCollected;
    private BigDecimal totalFeesDue;
    private BigDecimal totalDiscountGiven;
    private int totalTransactions;
    private List<MonthlyFeeSummary> monthlyCollections;
    private List<PaymentModeSummary> paymentModeSummary;
}
