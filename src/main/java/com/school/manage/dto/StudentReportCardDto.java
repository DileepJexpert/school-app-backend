package com.school.manage.dto;

import com.school.manage.model.CoscholasticAssessment;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Full student report card — groups all results for a student across
 * every exam type and subject for a given academic year, and appends
 * co-scholastic assessment and weighted cumulative GPA.
 */
@Data
public class StudentReportCardDto {

    private String studentId;
    private String studentName;
    private String className;
    private String rollNumber;
    private String academicYear;

    // ── Subject summaries ────────────────────────────────────────────────
    /** One entry per subject; contains marks for every exam type taken. */
    private List<SubjectSummary> subjects;

    // ── Overall performance ───────────────────────────────────────────────
    private double cumulativePercentage;    // weighted average across all exams
    private String overallGrade;
    private double overallGradePoint;
    private int    classRank;               // rank among class for cumulative %

    // ── Co-scholastic ────────────────────────────────────────────────────
    private CoscholasticAssessment coscholasticTerm1;
    private CoscholasticAssessment coscholasticTerm2;

    // ─────────────────────────────────────────────────────────────────────
    @Data
    public static class SubjectSummary {
        private String subject;
        /** examType → ExamResult */
        private Map<String, ExamResult> examResults;
        private double weightedPercentage;  // weighted cumulative for this subject
        private String predictedGrade;      // based on completed exams + weightage
        /** IMPROVING | DECLINING | STABLE */
        private String trend;
    }

    @Data
    public static class ExamResult {
        private double marksObtained;
        private double maxMarks;
        private double percentage;
        private String grade;
        private double gradePoint;
        private boolean isPassed;
        private int    classRank;
        private String teacherRemarks;
    }
}
