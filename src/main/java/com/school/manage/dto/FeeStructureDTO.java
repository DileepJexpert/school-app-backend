package com.school.manage.dto;

import java.math.BigDecimal;

public class FeeStructureDTO {
    private String className;
    private BigDecimal amount;
    // Add other fields as needed, e.g., tuition, transport, etc.

    // Getters and Setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
