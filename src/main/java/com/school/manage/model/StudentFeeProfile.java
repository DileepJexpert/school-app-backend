package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.List;

@Data
@Document(collection = "student_fee_profiles")
public class StudentFeeProfile {

    @Id
    private String id;
    private String name;
    private String className;
    private String rollNumber;
    private String parentName;

    private BigDecimal totalFees;
    private BigDecimal paidFees;
    private BigDecimal dueFees;

    /** --- NEW FIELD ---
     * Stores the total cumulative discount given to this student throughout the year.
     * Initialized to zero.
     */
    private BigDecimal totalDiscountGiven = BigDecimal.ZERO;

    private List<FeeInstallment> feeInstallments;
    private PaymentRecord lastPayment;

    // Backward-compatible getter
    public List<FeeInstallment> getMonthlyFees() {
        return this.feeInstallments;
    }
}
