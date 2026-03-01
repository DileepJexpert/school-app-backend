package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Per-academic-year exam configuration.
 * Defines weightage (%) for each exam type so the system can compute
 * a meaningful weighted cumulative GPA across the year.
 *
 * Example weightage scheme (must sum to 100):
 *   Unit Test 1  → 10 %
 *   Unit Test 2  → 10 %
 *   Mid Term     → 10 %
 *   Half Yearly  → 30 %
 *   Annual       → 40 %
 */
@Data
@Document(collection = "exam_configs")
public class ExamConfig {

    @Id
    private String id;

    private String academicYear;      // e.g. "2024-25"
    private String examType;          // UNIT_TEST_1 | UNIT_TEST_2 | MID_TERM |
                                      // HALF_YEARLY | ANNUAL | PRE_BOARD
    private String displayName;       // "Unit Test 1", "Half Yearly Examination"
    private int    weightagePercent;  // 0–100 (all configs for a year should sum to 100)
    private double maxMarksDefault;   // default max marks for this exam type
    private boolean isActive = true;

    private LocalDateTime createdAt;
}
