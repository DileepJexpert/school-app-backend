package com.school.manage.dto;

import lombok.Data;
import java.util.List;

/**
 * Rich analytics response for a class — powers the Analytics tab.
 *
 * Innovations over existing global systems:
 *  - Subject difficulty heatmap (class average per subject)
 *  - At-risk detection (failing OR declining > 15 % from last exam)
 *  - Recognition board: Topper, Most Improved, Most Consistent
 *  - Performance streak tracking
 */
@Data
public class ClassAnalyticsDto {

    private String className;
    private String examType;
    private String academicYear;

    // ── Summary stats ────────────────────────────────────────────────────
    private int    totalStudents;
    private double classAverage;        // avg % across all subjects
    private double highestPercentage;
    private double lowestPercentage;
    private double passPercentage;      // students with all subjects passed / total

    // ── Subject heatmap ──────────────────────────────────────────────────
    private List<SubjectAnalysis> subjectHeatmap;

    // ── At-risk students ─────────────────────────────────────────────────
    private List<AtRiskStudentInfo> atRiskStudents;

    // ── Recognition board ────────────────────────────────────────────────
    private List<RecognitionEntry> recognition;

    // ─────────────────────────────────────────────────────────────────────
    @Data
    public static class SubjectAnalysis {
        private String subject;
        private double classAverage;
        private double passPercentage;
        /** EXCELLENT (≥85 %) | GOOD (70–84 %) | AVERAGE (50–69 %) | WEAK (< 50 %) */
        private String performance;
        private int    totalStudents;
    }

    @Data
    public static class AtRiskStudentInfo {
        private String       studentId;
        private String       studentName;
        private String       rollNumber;
        private List<String> failedSubjects;
        private List<String> droppingSubjects;   // dropped > 15 % vs. previous exam
        private double       overallPercentage;
        /** CRITICAL (failing) | WARNING (at-risk zone 33–49 %) */
        private String       riskLevel;
    }

    @Data
    public static class RecognitionEntry {
        /** CLASS_TOPPER | MOST_IMPROVED | MOST_CONSISTENT */
        private String category;
        private String studentName;
        private String detail;         // e.g. "98.4 %", "+12.5 % improvement"
    }
}
