package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardAnalyticsResponse {
    private int todayPresent;
    private int todayAbsent;
    private int todayLate;
    private int todayHalfDay;
    private int todayTotal;

    private List<DailyAttendanceTrend> weeklyAttendance;

    private Map<String, Integer> genderDistribution;

    private int newAdmissionsThisMonth;
    private int newAdmissionsLastMonth;

    @Data
    @Builder
    public static class DailyAttendanceTrend {
        private String date;     // "Mon", "Tue", etc.
        private String fullDate; // "2026-06-16"
        private int present;
        private int absent;
        private int late;
        private int total;
    }
}
