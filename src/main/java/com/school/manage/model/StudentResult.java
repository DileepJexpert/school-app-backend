package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Normalised result document — one record per student, per exam, per subject.
 *
 * Grade scale: CBSE 10-point (A1 → E)
 *   A1 91-100 = 10 pts | A2 81-90 = 9 | B1 71-80 = 8 | B2 61-70 = 7
 *   C1 51-60  =  6 pts | C2 41-50 = 5 | D  33-40 = 4 | E  < 33 = 0 (FAIL)
 */
@Data
@Document(collection = "student_results")
public class StudentResult {

    @Id
    private String id;

    // ── Student info (denormalised for fast queries) ───────────────────────
    private String studentId;
    private String studentName;
    private String rollNumber;
    private String className;        // e.g. "Class 10 - A"

    // ── Exam info ─────────────────────────────────────────────────────────
    private String academicYear;     // e.g. "2024-25"
    private String examType;         // UNIT_TEST_1 | UNIT_TEST_2 | MID_TERM |
                                     // HALF_YEARLY | ANNUAL | PRE_BOARD
    private String subject;          // "Mathematics", "Physics" …

    // ── Marks ─────────────────────────────────────────────────────────────
    private double marksObtained;
    private double maxMarks;

    // ── Computed (set by ResultService) ───────────────────────────────────
    private double percentage;
    private String grade;            // A1, A2, B1, B2, C1, C2, D, E
    private double gradePoint;       // 10, 9, 8, 7, 6, 5, 4, 0
    private boolean isPassed;
    private int    classRank;        // rank in class for this exam+subject

    // ── Teacher feedback ──────────────────────────────────────────────────
    private String teacherRemarks;

    // ── Workflow ──────────────────────────────────────────────────────────
    private boolean isPublished = false;
    private String  enteredBy;

    // ── Timestamps ────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
