package com.school.manage.dto;

import lombok.Data;

import java.util.Map;

@Data
public class StaffDashboardDto {

    private long totalStaff;
    private long activeStaff;
    private long onLeaveToday;
    private long pendingLeaveRequests;

    /** Department name -> count */
    private Map<String, Long> departmentWise;

    private double totalMonthlyPayroll;
}
