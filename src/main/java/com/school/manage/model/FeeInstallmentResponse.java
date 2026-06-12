package com.school.manage.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for an individual fee installment within the StudentFeeProfileResponse.
 */
@Data
@Builder
public class FeeInstallmentResponse {
    private String installmentId;
    private String installmentName;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private LocalDate dueDate;
    private String status;
}
