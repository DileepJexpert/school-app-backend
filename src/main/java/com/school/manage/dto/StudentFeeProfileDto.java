package com.school.manage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeProfileDto {
    private String id;
    private String name;
    private String className;
    private String rollNumber;
    private String parentName;
    private List<FeeInstallmentDto> monthlyFees; // All installments for the session
    private PaymentRecordDto lastPayment; // Details of the last payment made

    // Calculated fields
    private double totalAnnualFeeEstimate;
    private double totalPaidInSession;
    private double currentSessionOutstanding;
    private String nextDueDate; // e.g., "July 2024" or "All Cleared"
}
