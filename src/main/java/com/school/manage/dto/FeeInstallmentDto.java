package com.school.manage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeInstallmentDto {
    private String monthYear;
    private double tuitionFee;
    private double transportFee;
    private double otherCharges;
    private double lateFineApplied;
    private boolean isPaid;
    // This is the fee including late fine, for display in selection
    private double totalMonthlyFeeWithFine;
    // This is the original fee without late fine, for calculations like total paid
    private double totalMonthlyFeeOriginal;
}