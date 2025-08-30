package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentModeSummary {
    private String paymentMode;      // e.g. "CASH", "CHEQUE", "DIGITAL"
    private BigDecimal totalAmount;  // total collected for this payment mode
}
