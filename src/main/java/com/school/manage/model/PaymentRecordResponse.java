package com.school.manage.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for the payment record response.
 * This defines the JSON object sent to the client after a payment.
 */
@Data
@Builder
public class PaymentRecordResponse {

    private String transactionId;
    private String studentId;
    private String studentName;
    private String receiptNumber;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;

    /** --- NEW FIELD ---
     * The discount amount applied in this transaction.
     */
    private BigDecimal discount;

    private String paymentMode;
    private List<String> paidForInstallments; // Matches the name used in your FeeService
    private String remarks;
}
