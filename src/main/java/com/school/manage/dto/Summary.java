package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class Summary {
    private BigDecimal totalCollected;
    private BigDecimal totalDue;
    private BigDecimal totalDiscountGiven;
    private int totalTransactions;
}
