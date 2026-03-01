package com.school.manage.controller;

import com.school.manage.dto.BulkResultRequest;
import com.school.manage.dto.ClassAnalyticsDto;
import com.school.manage.dto.StudentReportCardDto;
import com.school.manage.model.CoscholasticAssessment;
import com.school.manage.model.ExamConfig;
import com.school.manage.model.StudentResult;
import com.school.manage.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    // ── BULK MARKS ENTRY ──────────────────────────────────────────────────

    /**
     * POST /api/results/bulk
     * Submit marks for an entire class in one call.
     * Re-entry is supported — existing records for the same combination are replaced.
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<StudentResult>> bulkSaveResults(
            @RequestBody BulkResultRequest request) {
        return ResponseEntity.ok(resultService.bulkSaveResults(request));
    }

    // ── CLASS RESULT SHEET ────────────────────────────────────────────────

    /**
     * GET /api/results/class/{className}/exam/{examType}?year=2024-25
     * Returns result sheet for a class for a specific exam — sorted by rank.
     */
    @GetMapping("/class/{className}/exam/{examType}")
    public ResponseEntity<List<StudentResult>> getClassResultSheet(
            @PathVariable String className,
            @PathVariable String examType,
            @RequestParam String year) {
        return ResponseEntity.ok(
                resultService.getClassResultSheet(className, examType, year));
    }

    // ── STUDENT RESULTS (all exams) ───────────────────────────────────────

    /**
     * GET /api/results/student/{studentId}?year=2024-25
     * Returns all results for a student for the given academic year.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentResult>> getStudentResults(
            @PathVariable String studentId,
            @RequestParam(required = false) String year) {
        if (year != null && !year.isBlank()) {
            return ResponseEntity.ok(
                    resultService.getResultsForStudent(studentId).stream()
                            .filter(r -> year.equals(r.getAcademicYear()))
                            .toList());
        }
        return ResponseEntity.ok(resultService.getResultsForStudent(studentId));
    }

    // ── STUDENT REPORT CARD ───────────────────────────────────────────────

    /**
     * GET /api/results/student/{studentId}/report?year=2024-25
     * Full report card — all subjects × all exams, weighted GPA, co-scholastic.
     */
    @GetMapping("/student/{studentId}/report")
    public ResponseEntity<StudentReportCardDto> getStudentReportCard(
            @PathVariable String studentId,
            @RequestParam String year) {
        return ResponseEntity.ok(resultService.getStudentReportCard(studentId, year));
    }

    // ── CLASS ANALYTICS ───────────────────────────────────────────────────

    /**
     * GET /api/results/class/{className}/analytics?year=2024-25&examType=ANNUAL
     * Heatmap, at-risk list, recognition board, summary stats.
     * examType is optional — if omitted, aggregates across the whole year.
     */
    @GetMapping("/class/{className}/analytics")
    public ResponseEntity<ClassAnalyticsDto> getClassAnalytics(
            @PathVariable String className,
            @RequestParam String year,
            @RequestParam(required = false) String examType) {
        return ResponseEntity.ok(
                resultService.getClassAnalytics(className, year, examType));
    }

    // ── UPDATE / DELETE ───────────────────────────────────────────────────

    /** PUT /api/results/{id} — Update a single result record. */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResult> updateResult(
            @PathVariable String id,
            @RequestBody StudentResult result) {
        return ResponseEntity.ok(resultService.updateResult(id, result));
    }

    /** DELETE /api/results/{id} — Delete a single result record. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable String id) {
        resultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

    // ── PUBLISH ───────────────────────────────────────────────────────────

    /**
     * PUT /api/results/publish?className=Class 10 - A&examType=ANNUAL&year=2024-25
     * Publishes all draft results and auto-creates a parent notification.
     */
    @PutMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishResults(
            @RequestParam String className,
            @RequestParam String examType,
            @RequestParam String year) {
        return ResponseEntity.ok(
                resultService.publishResults(className, examType, year));
    }

    // ── EXAM CONFIG ───────────────────────────────────────────────────────

    /** GET /api/results/exam-config?year=2024-25 */
    @GetMapping("/exam-config")
    public ResponseEntity<List<ExamConfig>> getExamConfigs(@RequestParam String year) {
        return ResponseEntity.ok(resultService.getExamConfigs(year));
    }

    /** POST /api/results/exam-config */
    @PostMapping("/exam-config")
    public ResponseEntity<ExamConfig> saveExamConfig(@RequestBody ExamConfig config) {
        return ResponseEntity.ok(resultService.saveExamConfig(config));
    }

    // ── CO-SCHOLASTIC ─────────────────────────────────────────────────────

    /** POST /api/results/coscholastic */
    @PostMapping("/coscholastic")
    public ResponseEntity<CoscholasticAssessment> saveCoscholastic(
            @RequestBody CoscholasticAssessment assessment) {
        return ResponseEntity.ok(resultService.saveCoscholastic(assessment));
    }

    /** GET /api/results/coscholastic/student/{studentId}?year=2024-25 */
    @GetMapping("/coscholastic/student/{studentId}")
    public ResponseEntity<List<CoscholasticAssessment>> getCoscholastic(
            @PathVariable String studentId,
            @RequestParam String year) {
        return ResponseEntity.ok(resultService.getCoscholastic(studentId, year));
    }

    // ── LEGACY (backward compatible) ──────────────────────────────────────

    @PostMapping("/add")
    public StudentResult addResult(@RequestBody StudentResult studentResult) {
        return resultService.addResult(studentResult);
    }

    @GetMapping("/class/{className}/year/{academicYear}")
    public List<StudentResult> getResultsByClassAndYear(
            @PathVariable String className,
            @PathVariable String academicYear) {
        return resultService.getResultsByClassAndYear(className, academicYear);
    }
}
