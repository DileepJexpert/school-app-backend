package com.school.manage.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for the student fee profile response. Defines the JSON structure
 * sent to the client, separating the API from the internal database model.
 */
@Data
@Builder
public class StudentFeeProfileResponse {
    private String id;
    private String name;
    private String className;
    private String rollNumber;
    private String parentName;
    private BigDecimal totalFees;
    private BigDecimal paidFees;
    private BigDecimal dueFees;

    /** --- NEW FIELD ---
     * The total cumulative discount given to this student.
     */
    private BigDecimal totalDiscountGiven;

    private List<FeeInstallmentResponse> feeInstallments;
    private PaymentRecordResponse lastPayment;
}
