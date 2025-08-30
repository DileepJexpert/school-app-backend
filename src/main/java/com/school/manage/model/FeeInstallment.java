package com.school.manage.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single, specific fee obligation for a student, embedded within a StudentFeeProfile.
 * This class is designed to be flexible enough to handle monthly fees (e.g., tuition)
 * as well as yearly or one-time fees (e.g., annual fee, admission fee).
 * As an embedded object, it does not need @Document or @Id annotations.
 */
@Data
public class FeeInstallment {

    /**
     * A unique identifier for this specific installment instance.
     * This is crucial for accurately updating the status of a single fee,
     * especially when a student has multiple fees due in the same month.
     * It's automatically generated when the installment is created.
     */
    private String installmentId = UUID.randomUUID().toString();

    /**
     * A descriptive name for the fee installment.
     * Examples: "Tuition Fee - JULY", "Annual Fee", "Admission Fee".
     * This replaces the simple 'month' field for greater clarity.
     */
    private String installmentName;

    /**
     * The total amount that is due for this specific installment.
     */
    private BigDecimal amountDue;

    /**
     * The amount that has been paid towards this installment so far.
     * Initialized to zero. This allows for tracking partial payments.
     */
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /**
     * The date by which this installment should be paid.
     * This is essential for determining late fees and payment deadlines.
     */
    private LocalDate dueDate;

    /**
     * The current payment status of the installment.
     * Examples: "PENDING", "PAID", "PARTIALLY_PAID".
     * This provides more detail than a simple boolean 'isPaid'.
     */
    private String status;
}
