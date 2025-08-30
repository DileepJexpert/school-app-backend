package com.school.manage.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Represents a single component of a fee structure (e.g., Tuition Fee, Annual Fee).
 * This class is intended to be embedded within the FeeStructure document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeComponent {

    /**
     * The name of the fee component.
     * Example: "Tuition Fee", "Transport Fee", "Annual Development Fee"
     */
    private String feeName;

    /**
     * --- CORRECTED ---
     * The monetary amount for this specific fee component.
     * Using BigDecimal is the standard practice for financial data to avoid
     * floating-point inaccuracies that can occur with Double.
     */
    private BigDecimal amount;

    /**
     * The frequency at which this fee is charged.
     * Expected values: "MONTHLY", "YEARLY", "ONE_TIME".
     */
    private String frequency;

    /**
     * An optional description for the fee component for admin reference.
     */
    private String description;
}
