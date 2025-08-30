package com.school.manage.model;

import com.school.manage.enums.PaymentMode;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentModeSummary {
    private PaymentMode paymentMode;
    private BigDecimal totalAmount;
}
