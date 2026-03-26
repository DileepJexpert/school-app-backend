package com.school.manage.dto;

import lombok.Data;

@Data
public class ChildOverviewDto {

    private String studentId;
    private String studentName;
    private String className;
    private String rollNumber;
    private String admissionNumber;

    private double attendancePercentage;
    private long totalPresent;
    private long totalAbsent;

    private double overallPercentage;
    private String overallGrade;

    private double totalFees;
    private double paidFees;
    private double pendingFees;
}
