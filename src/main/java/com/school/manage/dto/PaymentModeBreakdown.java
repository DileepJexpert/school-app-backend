package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentModeBreakdown {
    private String paymentMode;        // e.g. CASH, CHEQUE, DIGITAL
    private BigDecimal totalAmount;    // sum of amountPaid for this mode
    private BigDecimal totalDiscount;  // sum of discounts for this mode
    private int transactionCount;      // number of transactions for this mode
}
