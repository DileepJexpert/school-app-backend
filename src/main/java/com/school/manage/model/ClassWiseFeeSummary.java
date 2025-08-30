package com.school.manage.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClassWiseFeeSummary {
    private String className;
    private BigDecimal totalCollectedInClass;
    private List<PaymentModeSummary> paymentModeBreakdown;
}
