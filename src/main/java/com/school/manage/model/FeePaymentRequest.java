package com.school.manage.model;

import com.school.manage.enums.PaymentMode;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FeePaymentRequest {

    @NotBlank(message = "Student ID cannot be blank.")
    private String studentId;

    @NotNull(message = "Amount cannot be null.")
    @PositiveOrZero(message = "Amount must be positive or zero.")
    private BigDecimal amount;

    /** --- NEW FIELD ---
     * The discount amount being applied in this transaction.
     */
    @NotNull(message = "Discount cannot be null.")
    @PositiveOrZero(message = "Discount must be positive or zero.")
    private BigDecimal discount;

    @NotEmpty(message = "At least one installment must be selected.")
    private List<String> installmentNames;

    @NotNull(message = "Payment mode must be specified.")
    private PaymentMode paymentMode;

    private String remarks;
    private String chequeDetails;
    private String transactionId;
}
