package com.school.manage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class FeePaymentRequestDto {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotEmpty(message = "At least one month must be selected for payment")
    private List<String> selectedMonthsToPay; // List of monthYear strings, e.g., ["June 2024", "July 2024"]

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // "Cash", "Cheque", "Digital Payment", "Challan"

    @PositiveOrZero(message = "Discount amount cannot be negative")
    private double discountAmount;

    private String remarks;
    private String chequeDetails; // Required if paymentMode is "Cheque"
    private String transactionId; // Required if paymentMode is "Digital Payment"

    // Calculated amounts from Flutter, can be validated on backend
    @NotNull
    private Double netAmountPaid; // The final amount the user is paying
}