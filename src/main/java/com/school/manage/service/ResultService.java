package com.school.manage.service;

import com.school.manage.dto.BulkResultRequest;
import com.school.manage.dto.ClassAnalyticsDto;
import com.school.manage.dto.StudentReportCardDto;
import com.school.manage.model.CoscholasticAssessment;
import com.school.manage.model.ExamConfig;
import com.school.manage.model.Notification;
import com.school.manage.model.StudentResult;
import com.school.manage.repository.CoscholasticRepository;
import com.school.manage.repository.ExamConfigRepository;
import com.school.manage.repository.StudentResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final StudentResultRepository resultRepository;
    private final ExamConfigRepository    examConfigRepository;
    private final CoscholasticRepository  coscholasticRepository;
    private final NotificationService     notificationService;

    // ── Exam ordering for "previous exam" comparisons ─────────────────────
    private static final List<String> EXAM_ORDER = List.of(
            "UNIT_TEST_1", "UNIT_TEST_2", "MID_TERM", "HALF_YEARLY", "ANNUAL", "PRE_BOARD");

    // ─────────────────────────────────────────────────────────────────────
    // GRADE COMPUTATION (CBSE 10-point scale)
    // ─────────────────────────────────────────────────────────────────────

    private String computeGrade(double pct) {
        if (pct >= 91) return "A1";
        if (pct >= 81) return "A2";
        if (pct >= 71) return "B1";
        if (pct >= 61) return "B2";
        if (pct >= 51) return "C1";
        if (pct >= 41) return "C2";
        if (pct >= 33) return "D";
        return "E";
    }

    private double computeGradePoint(double pct) {
        if (pct >= 91) return 10.0;
        if (pct >= 81) return  9.0;
        if (pct >= 71) return  8.0;
        if (pct >= 61) return  7.0;
        if (pct >= 51) return  6.0;
        if (pct >= 41) return  5.0;
        if (pct >= 33) return  4.0;
        return 0.0;
    }

    /** Round to 2 decimal places. */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private void fillComputed(StudentResult r) {
        double pct = r.getMaxMarks() > 0
                ? round2((r.getMarksObtained() / r.getMaxMarks()) * 100.0)
                : 0.0;
        r.setPercentage(pct);
        r.setGrade(computeGrade(pct));
        r.setGradePoint(computeGradePoint(pct));
        r.setIsPassed(pct >= 33.0);
    }

    // ─────────────────────────────────────────────────────────────────────
    // RANK COMPUTATION
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Recomputes and saves class ranks for every student in a given
     * class / academicYear / examType / subject combination.
     * Ties share the same rank; the next rank is incremented accordingly.
     */
    private void recomputeRanks(String className, String academicYear,
                                 String examType, String subject) {
        List<StudentResult> results = resultRepository
                .findByClassNameAndAcademicYearAndExamTypeAndSubject(
                        className, academicYear, examType, subject);

        results.sort(Comparator.comparingDouble(StudentResult::getMarksObtained).reversed());

        int rank = 1;
        for (int i = 0; i < results.size(); i++) {
            if (i > 0 && results.get(i).getMarksObtained() < results.get(i - 1).getMarksObtained()) {
                rank = i + 1;
            }
            results.get(i).setClassRank(rank);
        }
        resultRepository.saveAll(results);
    }

    // ─────────────────────────────────────────────────────────────────────
    // BULK SAVE
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Replaces all results for a class + examType + subject + academicYear
     * with the new entries, computes grades, saves, then recomputes ranks.
     */
    public List<StudentResult> bulkSaveResults(BulkResultRequest req) {
        // Delete previous entries for this combination (re-entry support)
        resultRepository.deleteByClassNameAndAcademicYearAndExamTypeAndSubject(
                req.getClassName(), req.getAcademicYear(),
                req.getExamType(), req.getSubject());

        LocalDateTime now = LocalDateTime.now();
        List<StudentResult> toSave = new ArrayList<>();

        for (BulkResultRequest.StudentEntry entry : req.getEntries()) {
            StudentResult r = new StudentResult();
            r.setStudentId(entry.getStudentId());
            r.setStudentName(entry.getStudentName());
            r.setRollNumber(entry.getRollNumber());
            r.setClassName(req.getClassName());
            r.setAcademicYear(req.getAcademicYear());
            r.setExamType(req.getExamType());
            r.setSubject(req.getSubject());
            r.setMarksObtained(entry.getMarksObtained());
            r.setMaxMarks(req.getMaxMarks());
            r.setTeacherRemarks(entry.getTeacherRemarks());
            r.setEnteredBy(req.getEnteredBy());
            r.setPublished(false);
            r.setCreatedAt(now);
            r.setUpdatedAt(now);
            fillComputed(r);
            toSave.add(r);
        }

        List<StudentResult> saved = resultRepository.saveAll(toSave);
        recomputeRanks(req.getClassName(), req.getAcademicYear(),
                req.getExamType(), req.getSubject());

        return resultRepository.findByClassNameAndAcademicYearAndExamTypeAndSubject(
                req.getClassName(), req.getAcademicYear(),
                req.getExamType(), req.getSubject());
    }

    // ─────────────────────────────────────────────────────────────────────
    // CLASS RESULT SHEET
    // ─────────────────────────────────────────────────────────────────────

    public List<StudentResult> getClassResultSheet(String className,
                                                    String examType,
                                                    String academicYear) {
        List<StudentResult> results = resultRepository
                .findByClassNameAndAcademicYearAndExamType(className, academicYear, examType);
        results.sort(Comparator.comparingInt(StudentResult::getClassRank));
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────
    // STUDENT REPORT CARD
    // ─────────────────────────────────────────────────────────────────────

    public StudentReportCardDto getStudentReportCard(String studentId, String academicYear) {
        List<StudentResult> allResults =
                resultRepository.findByStudentIdAndAcademicYear(studentId, academicYear);

        if (allResults.isEmpty()) {
            StudentReportCardDto empty = new StudentReportCardDto();
            empty.setStudentId(studentId);
            empty.setAcademicYear(academicYear);
            empty.setSubjects(Collections.emptyList());
            return empty;
        }

        // Metadata from first result
        StudentResult first = allResults.get(0);
        StudentReportCardDto card = new StudentReportCardDto();
        card.setStudentId(studentId);
        card.setStudentName(first.getStudentName());
        card.setClassName(first.getClassName());
        card.setRollNumber(first.getRollNumber());
        card.setAcademicYear(academicYear);

        // Load exam configs for weighted GPA
        List<ExamConfig> configs =
                examConfigRepository.findByAcademicYearAndIsActive(academicYear, true);
        Map<String, Integer> weightMap = configs.stream()
                .collect(Collectors.toMap(ExamConfig::getExamType,
                        ExamConfig::getWeightagePercent, (a, b) -> a));

        // Group by subject
        Map<String, List<StudentResult>> bySubject = allResults.stream()
                .collect(Collectors.groupingBy(StudentResult::getSubject));

        List<StudentReportCardDto.SubjectSummary> subjects = new ArrayList<>();
        double totalWeightedPct = 0.0;
        int totalWeight = 0;

        for (Map.Entry<String, List<StudentResult>> entry : bySubject.entrySet()) {
            StudentReportCardDto.SubjectSummary ss = new StudentReportCardDto.SubjectSummary();
            ss.setSubject(entry.getKey());

            Map<String, StudentReportCardDto.ExamResult> examResults = new LinkedHashMap<>();
            List<Double> percentages = new ArrayList<>();

            // Sorted by exam order
            List<StudentResult> subjectResults = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(r -> EXAM_ORDER.indexOf(r.getExamType())))
                    .collect(Collectors.toList());

            for (StudentResult r : subjectResults) {
                StudentReportCardDto.ExamResult er = new StudentReportCardDto.ExamResult();
                er.setMarksObtained(r.getMarksObtained());
                er.setMaxMarks(r.getMaxMarks());
                er.setPercentage(r.getPercentage());
                er.setGrade(r.getGrade());
                er.setGradePoint(r.getGradePoint());
                er.setPassed(r.isPassed());
                er.setClassRank(r.getClassRank());
                er.setTeacherRemarks(r.getTeacherRemarks());
                examResults.put(r.getExamType(), er);
                percentages.add(r.getPercentage());
            }
            ss.setExamResults(examResults);

            // Weighted cumulative for this subject
            double subjectWeightedPct = 0.0;
            int subjectWeight = 0;
            for (StudentResult r : subjectResults) {
                int w = weightMap.getOrDefault(r.getExamType(), 0);
                subjectWeightedPct += r.getPercentage() * w;
                subjectWeight += w;
            }
            double subjectCumulative = subjectWeight > 0
                    ? round2(subjectWeightedPct / subjectWeight)
                    : round2(percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            ss.setWeightedPercentage(subjectCumulative);
            ss.setPredictedGrade(computeGrade(subjectCumulative));

            // Trend (compare last two exams)
            if (percentages.size() >= 2) {
                double delta = percentages.get(percentages.size() - 1)
                        - percentages.get(percentages.size() - 2);
                ss.setTrend(delta > 2 ? "IMPROVING" : delta < -2 ? "DECLINING" : "STABLE");
            } else {
                ss.setTrend("STABLE");
            }

            totalWeightedPct += subjectCumulative;
            totalWeight++;
            subjects.add(ss);
        }

        subjects.sort(Comparator.comparing(StudentReportCardDto.SubjectSummary::getSubject));
        card.setSubjects(subjects);

        double cumPct = totalWeight > 0 ? round2(totalWeightedPct / totalWeight) : 0;
        card.setCumulativePercentage(cumPct);
        card.setOverallGrade(computeGrade(cumPct));
        card.setOverallGradePoint(computeGradePoint(cumPct));

        // Compute class rank based on cumulative % among classmates
        List<StudentResult> classResults =
                resultRepository.findByClassNameAndAcademicYear(first.getClassName(), academicYear);
        Map<String, Double> studentAvgMap = classResults.stream()
                .collect(Collectors.groupingBy(StudentResult::getStudentId,
                        Collectors.averagingDouble(StudentResult::getPercentage)));
        long betterStudents = studentAvgMap.values().stream()
                .filter(avg -> avg > cumPct).count();
        card.setClassRank((int) betterStudents + 1);

        // Co-scholastic
        List<CoscholasticAssessment> coscho =
                coscholasticRepository.findByStudentIdAndAcademicYear(studentId, academicYear);
        coscho.forEach(c -> {
            if ("TERM_1".equals(c.getTerm())) card.setCoscholasticTerm1(c);
            else if ("TERM_2".equals(c.getTerm())) card.setCoscholasticTerm2(c);
        });

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CLASS ANALYTICS
    // ─────────────────────────────────────────────────────────────────────

    public ClassAnalyticsDto getClassAnalytics(String className,
                                                String academicYear,
                                                String examType) {
        List<StudentResult> results = (examType != null && !examType.isBlank())
                ? resultRepository.findByClassNameAndAcademicYearAndExamType(
                        className, academicYear, examType)
                : resultRepository.findByClassNameAndAcademicYear(className, academicYear);

        ClassAnalyticsDto dto = new ClassAnalyticsDto();
        dto.setClassName(className);
        dto.setExamType(examType);
        dto.setAcademicYear(academicYear);

        if (results.isEmpty()) {
            dto.setTotalStudents(0);
            dto.setSubjectHeatmap(Collections.emptyList());
            dto.setAtRiskStudents(Collections.emptyList());
            dto.setRecognition(Collections.emptyList());
            return dto;
        }

        // Distinct students
        Set<String> studentIds = results.stream()
                .map(StudentResult::getStudentId).collect(Collectors.toSet());
        dto.setTotalStudents(studentIds.size());

        // ── Per-student aggregate ─────────────────────────────────────────
        Map<String, List<StudentResult>> byStudent = results.stream()
                .collect(Collectors.groupingBy(StudentResult::getStudentId));

        Map<String, Double> studentAvg = new HashMap<>();
        Map<String, String> studentName = new HashMap<>();
        Map<String, Boolean> allPassed  = new HashMap<>();

        for (Map.Entry<String, List<StudentResult>> e : byStudent.entrySet()) {
            double avg = e.getValue().stream()
                    .mapToDouble(StudentResult::getPercentage).average().orElse(0);
            studentAvg.put(e.getKey(), round2(avg));
            studentName.put(e.getKey(), e.getValue().get(0).getStudentName());
            boolean passed = e.getValue().stream().allMatch(StudentResult::isPassed);
            allPassed.put(e.getKey(), passed);
        }

        double classAvg = round2(studentAvg.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0));
        dto.setClassAverage(classAvg);

        double highest = round2(studentAvg.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(0));
        double lowest = round2(studentAvg.values().stream()
                .mapToDouble(Double::doubleValue).min().orElse(0));
        dto.setHighestPercentage(highest);
        dto.setLowestPercentage(lowest);

        long passCount = allPassed.values().stream().filter(Boolean::booleanValue).count();
        dto.setPassPercentage(round2((double) passCount / studentIds.size() * 100));

        // ── Subject heatmap ───────────────────────────────────────────────
        Map<String, List<StudentResult>> bySubject = results.stream()
                .collect(Collectors.groupingBy(StudentResult::getSubject));

        List<ClassAnalyticsDto.SubjectAnalysis> heatmap = new ArrayList<>();
        for (Map.Entry<String, List<StudentResult>> e : bySubject.entrySet()) {
            ClassAnalyticsDto.SubjectAnalysis sa = new ClassAnalyticsDto.SubjectAnalysis();
            sa.setSubject(e.getKey());
            sa.setTotalStudents(e.getValue().size());
            double subAvg = round2(e.getValue().stream()
                    .mapToDouble(StudentResult::getPercentage).average().orElse(0));
            sa.setClassAverage(subAvg);
            long subPass = e.getValue().stream().filter(StudentResult::isPassed).count();
            sa.setPassPercentage(round2((double) subPass / e.getValue().size() * 100));
            sa.setPerformance(subAvg >= 85 ? "EXCELLENT"
                    : subAvg >= 70 ? "GOOD"
                    : subAvg >= 50 ? "AVERAGE" : "WEAK");
            heatmap.add(sa);
        }
        heatmap.sort(Comparator.comparing(ClassAnalyticsDto.SubjectAnalysis::getSubject));
        dto.setSubjectHeatmap(heatmap);

        // ── At-risk students ──────────────────────────────────────────────
        // Pre-fetch previous exam results once (keyed by studentId → subject → pct)
        // to avoid N queries inside the per-student loop.
        Map<String, Map<String, Double>> prevExamPctByStudent = new HashMap<>();
        if (examType != null && !examType.isBlank()) {
            int idx = EXAM_ORDER.indexOf(examType);
            if (idx > 0) {
                String prevExam = EXAM_ORDER.get(idx - 1);
                List<StudentResult> prevAll = resultRepository
                        .findByClassNameAndAcademicYearAndExamType(
                                className, academicYear, prevExam);
                for (StudentResult pr : prevAll) {
                    prevExamPctByStudent
                            .computeIfAbsent(pr.getStudentId(), k -> new HashMap<>())
                            .put(pr.getSubject(), pr.getPercentage());
                }
            }
        }

        List<ClassAnalyticsDto.AtRiskStudentInfo> atRisk = new ArrayList<>();
        for (Map.Entry<String, List<StudentResult>> e : byStudent.entrySet()) {
            List<String> failedSubs = e.getValue().stream()
                    .filter(r -> !r.isPassed())
                    .map(StudentResult::getSubject)
                    .collect(Collectors.toList());

            // Detect subjects where student dropped > 15 % vs. previous exam
            List<String> droppingSubs = new ArrayList<>();
            Map<String, Double> prevSubPct =
                    prevExamPctByStudent.getOrDefault(e.getKey(), Collections.emptyMap());
            for (StudentResult curr : e.getValue()) {
                Double prev = prevSubPct.get(curr.getSubject());
                if (prev != null && (prev - curr.getPercentage()) > 15) {
                    droppingSubs.add(curr.getSubject());
                }
            }

            double avg = studentAvg.getOrDefault(e.getKey(), 0.0);
            if (!failedSubs.isEmpty() || !droppingSubs.isEmpty() || avg < 50) {
                ClassAnalyticsDto.AtRiskStudentInfo ar = new ClassAnalyticsDto.AtRiskStudentInfo();
                ar.setStudentId(e.getKey());
                ar.setStudentName(studentName.get(e.getKey()));
                ar.setRollNumber(e.getValue().get(0).getRollNumber());
                ar.setFailedSubjects(failedSubs);
                ar.setDroppingSubjects(droppingSubs);
                ar.setOverallPercentage(avg);
                ar.setRiskLevel(!failedSubs.isEmpty() ? "CRITICAL" : "WARNING");
                atRisk.add(ar);
            }
        }
        atRisk.sort(Comparator.comparingDouble(ClassAnalyticsDto.AtRiskStudentInfo::getOverallPercentage));
        dto.setAtRiskStudents(atRisk);

        // ── Recognition board ─────────────────────────────────────────────
        List<ClassAnalyticsDto.RecognitionEntry> recognition = new ArrayList<>();

        // Class Topper
        studentAvg.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(top -> {
                    ClassAnalyticsDto.RecognitionEntry re = new ClassAnalyticsDto.RecognitionEntry();
                    re.setCategory("CLASS_TOPPER");
                    re.setStudentName(studentName.get(top.getKey()));
                    re.setDetail(top.getValue() + " %");
                    recognition.add(re);
                });

        // Most Improved (highest improvement vs. previous exam for same class)
        if (examType != null && !examType.isBlank()) {
            int idx = EXAM_ORDER.indexOf(examType);
            if (idx > 0) {
                String prevExam = EXAM_ORDER.get(idx - 1);
                List<StudentResult> prevAll = resultRepository
                        .findByClassNameAndAcademicYearAndExamType(className, academicYear, prevExam);
                Map<String, Double> prevAvg = prevAll.stream()
                        .collect(Collectors.groupingBy(StudentResult::getStudentId,
                                Collectors.averagingDouble(StudentResult::getPercentage)));

                String mostImprovedId = null;
                double maxDelta = 0;
                for (String sid : studentIds) {
                    if (prevAvg.containsKey(sid)) {
                        double delta = studentAvg.get(sid) - prevAvg.get(sid);
                        if (delta > maxDelta) { maxDelta = delta; mostImprovedId = sid; }
                    }
                }
                if (mostImprovedId != null && maxDelta > 0) {
                    ClassAnalyticsDto.RecognitionEntry re = new ClassAnalyticsDto.RecognitionEntry();
                    re.setCategory("MOST_IMPROVED");
                    re.setStudentName(studentName.get(mostImprovedId));
                    re.setDetail("+" + round2(maxDelta) + " % improvement");
                    recognition.add(re);
                }
            }
        }

        // Most Consistent (lowest std-dev across subjects)
        String mostConsistentId = null;
        double minVariance = Double.MAX_VALUE;
        for (Map.Entry<String, List<StudentResult>> e : byStudent.entrySet()) {
            if (e.getValue().size() < 2) continue;
            double avg = studentAvg.get(e.getKey());
            double variance = e.getValue().stream()
                    .mapToDouble(r -> Math.pow(r.getPercentage() - avg, 2))
                    .average().orElse(Double.MAX_VALUE);
            if (variance < minVariance) { minVariance = variance; mostConsistentId = e.getKey(); }
        }
        if (mostConsistentId != null) {
            ClassAnalyticsDto.RecognitionEntry re = new ClassAnalyticsDto.RecognitionEntry();
            re.setCategory("MOST_CONSISTENT");
            re.setStudentName(studentName.get(mostConsistentId));
            re.setDetail("Std Dev: " + round2(Math.sqrt(minVariance)) + " %");
            recognition.add(re);
        }

        dto.setRecognition(recognition);
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PUBLISH RESULTS  (draft → published + auto-notification)
    // ─────────────────────────────────────────────────────────────────────

    public Map<String, Object> publishResults(String className,
                                               String examType,
                                               String academicYear) {
        List<StudentResult> results = resultRepository
                .findByClassNameAndAcademicYearAndExamTypeAndIsPublished(
                        className, academicYear, examType, false);

        results.forEach(r -> {
            r.setPublished(true);
            r.setUpdatedAt(LocalDateTime.now());
        });
        resultRepository.saveAll(results);

        // Auto-create a class-specific notification
        String displayExam = examType.replace("_", " ");
        Notification n = new Notification();
        n.setTitle("Results Published — " + displayExam);
        n.setMessage(className + ": " + displayExam + " (" + academicYear
                + ") results are now available. Please check your ward's performance.");
        n.setType("EXAM");
        n.setTargetAudience("CLASS_SPECIFIC");
        n.setTargetClass(className);
        n.setPriority("HIGH");
        n.setCreatedBy("SYSTEM");
        notificationService.createNotification(n);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("published", results.size());
        resp.put("className", className);
        resp.put("examType", examType);
        resp.put("academicYear", academicYear);
        resp.put("notificationSent", true);
        return resp;
    }

    // ─────────────────────────────────────────────────────────────────────
    // UPDATE / DELETE
    // ─────────────────────────────────────────────────────────────────────

    public StudentResult updateResult(String id, StudentResult updated) {
        StudentResult existing = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Result not found: " + id));
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        fillComputed(updated);
        StudentResult saved = resultRepository.save(updated);
        recomputeRanks(saved.getClassName(), saved.getAcademicYear(),
                saved.getExamType(), saved.getSubject());
        return saved;
    }

    public void deleteResult(String id) {
        StudentResult r = resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Result not found: " + id));
        resultRepository.deleteById(id);
        recomputeRanks(r.getClassName(), r.getAcademicYear(),
                r.getExamType(), r.getSubject());
    }

    // ─────────────────────────────────────────────────────────────────────
    // EXAM CONFIG
    // ─────────────────────────────────────────────────────────────────────

    public List<ExamConfig> getExamConfigs(String academicYear) {
        return examConfigRepository.findByAcademicYearAndIsActive(academicYear, true);
    }

    public ExamConfig saveExamConfig(ExamConfig config) {
        config.setCreatedAt(LocalDateTime.now());
        return examConfigRepository.save(config);
    }

    // ─────────────────────────────────────────────────────────────────────
    // CO-SCHOLASTIC
    // ─────────────────────────────────────────────────────────────────────

    public CoscholasticAssessment saveCoscholastic(CoscholasticAssessment assessment) {
        // Upsert: replace existing for same student+year+term
        coscholasticRepository.findByStudentIdAndAcademicYearAndTerm(
                assessment.getStudentId(), assessment.getAcademicYear(), assessment.getTerm())
                .ifPresent(existing -> assessment.setId(existing.getId()));
        assessment.setCreatedAt(LocalDateTime.now());
        return coscholasticRepository.save(assessment);
    }

    public List<CoscholasticAssessment> getCoscholastic(String studentId, String academicYear) {
        return coscholasticRepository.findByStudentIdAndAcademicYear(studentId, academicYear);
    }

    // ─────────────────────────────────────────────────────────────────────
    // LEGACY SUPPORT
    // ─────────────────────────────────────────────────────────────────────

    public StudentResult addResult(StudentResult r) {
        fillComputed(r);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return resultRepository.save(r);
    }

    public List<StudentResult> getResultsByClassAndYear(String className, int year) {
        return resultRepository.findByClassNameAndYear(className, year);
    }

    public List<StudentResult> getResultsForStudent(String studentId) {
        return resultRepository.findByStudentId(studentId);
    }
}
