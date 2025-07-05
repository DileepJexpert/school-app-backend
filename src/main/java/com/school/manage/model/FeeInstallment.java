package com.school.manage.model;

import lombok.Data;

@Data
public class FeeInstallment {
    private String monthYear; // e.g., "April 2024"
    private double tuitionFee;
    private double transportFee;
    private double otherCharges;
    private double lateFineApplied;
    private boolean isPaid;
    private String paymentId; // Optional: Link to the PaymentRecord that paid this
}