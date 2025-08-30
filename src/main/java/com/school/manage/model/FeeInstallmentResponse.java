package com.school.manage.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for an individual fee installment within the StudentFeeProfileResponse.
 */
@Data
@Builder
public class FeeInstallmentResponse {
    private String installmentName;
    private BigDecimal amountDue;
    private String status;
}
