package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MonthlyFeeSummary {
    private int month;      // 1 = January, 4 = April …
    private int year;
    private String label;   // "Apr", "May", …
    private BigDecimal amount;
}
