package com.school.manage.dto;

import lombok.Data;

@Data
public class AttendanceSummaryDto {
    private String studentId;
    private String studentName;
    private String className;
    private String academicYear;
    private long totalDays;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private long halfDays;
    private double attendancePercentage;
}
