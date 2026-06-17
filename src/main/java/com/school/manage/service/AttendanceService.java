package com.school.manage.service;

import com.school.manage.dto.AttendanceRequestDto;
import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Attendance;
import com.school.manage.repository.AttendanceRepository;
import com.school.manage.tenant.TenantContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ParentNotificationService parentNotificationService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             @Lazy ParentNotificationService parentNotificationService) {
        this.attendanceRepository = attendanceRepository;
        this.parentNotificationService = parentNotificationService;
    }

    /**
     * Marks attendance for multiple students in a class for a given date.
     * If a record already exists for a student on that date, it will be updated.
     */
    public List<Attendance> markBulkAttendance(AttendanceRequestDto request) {
        List<Attendance> savedRecords = new ArrayList<>();

        for (AttendanceRequestDto.StudentAttendanceEntry entry : request.getEntries()) {
            // Check if attendance already marked for this student on this date
            Attendance attendance = attendanceRepository
                    .findByStudentIdAndDate(entry.getStudentId(), request.getDate())
                    .orElse(new Attendance());

            attendance.setStudentId(entry.getStudentId());
            attendance.setStudentName(entry.getStudentName());
            attendance.setClassName(request.getClassName());
            attendance.setAcademicYear(request.getAcademicYear());
            attendance.setDate(request.getDate());
            attendance.setStatus(entry.getStatus());
            attendance.setRemarks(entry.getRemarks());
            attendance.setMarkedBy(request.getMarkedBy());
            attendance.setMarkedAt(LocalDateTime.now());

            savedRecords.add(attendanceRepository.save(attendance));
        }

        parentNotificationService.sendAbsenceAlerts(savedRecords, TenantContext.getTenant());

        return savedRecords;
    }

    /**
     * Returns attendance records for a specific student between two dates.
     */
    public List<Attendance> getAttendanceByStudentAndDateRange(
            String studentId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }

    /**
     * Returns all attendance records for a class on a specific date.
     */
    public List<Attendance> getAttendanceByClassAndDate(String className, LocalDate date) {
        return attendanceRepository.findByClassNameAndDate(className, date);
    }

    /**
     * Returns a summary of attendance for a student in an academic year.
     */
    public AttendanceSummaryDto getAttendanceSummary(String studentId, String academicYear) {
        List<Attendance> records = attendanceRepository
                .findByStudentIdAndAcademicYear(studentId, academicYear);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No attendance records found for student: " + studentId);
        }

        AttendanceSummaryDto summary = new AttendanceSummaryDto();
        summary.setStudentId(studentId);
        summary.setStudentName(records.get(0).getStudentName());
        summary.setClassName(records.get(0).getClassName());
        summary.setAcademicYear(academicYear);
        summary.setTotalDays(records.size());
        summary.setPresentDays(records.stream().filter(a -> "PRESENT".equals(a.getStatus())).count());
        summary.setAbsentDays(records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count());
        summary.setLateDays(records.stream().filter(a -> "LATE".equals(a.getStatus())).count());
        summary.setHalfDays(records.stream().filter(a -> "HALF_DAY".equals(a.getStatus())).count());

        double percentage = records.isEmpty() ? 0 :
                ((double) (summary.getPresentDays() + summary.getLateDays()) / summary.getTotalDays()) * 100;
        summary.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);

        return summary;
    }

    /**
     * Returns all attendance records for a class within a date range.
     */
    public List<Attendance> getAttendanceByClassAndDateRange(
            String className, String academicYear, LocalDate from, LocalDate to) {
        return attendanceRepository.findByClassNameAndAcademicYearAndDateBetween(
                className, academicYear, from, to);
    }

    /**
     * Returns a single attendance record by ID.
     */
    public Attendance getAttendanceById(String id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));
    }

    /**
     * Deletes an attendance record by ID.
     */
    public void deleteAttendance(String id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance record not found: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    /**
     * Returns comprehensive attendance analytics for an academic year,
     * optionally filtered by class name.
     */
    public Map<String, Object> getAttendanceAnalytics(String className, String academicYear) {
        List<Attendance> records;
        if (className != null && !className.isEmpty()) {
            records = attendanceRepository.findByClassNameAndAcademicYear(className, academicYear);
        } else {
            records = attendanceRepository.findByAcademicYear(academicYear);
        }

        Map<String, Object> analytics = new LinkedHashMap<>();

        // 1. Overall attendance percentage
        long totalRecords = records.size();
        long presentCount = records.stream()
                .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                .count();
        double overallPercentage = totalRecords == 0 ? 0.0
                : Math.round(((double) presentCount / totalRecords) * 10000.0) / 100.0;
        analytics.put("overallAttendancePercentage", overallPercentage);

        // 2. Class-wise breakdown
        Map<String, List<Attendance>> byClass = records.stream()
                .collect(Collectors.groupingBy(Attendance::getClassName));
        List<Map<String, Object>> classWiseBreakdown = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : byClass.entrySet()) {
            List<Attendance> classRecords = entry.getValue();
            long classPresent = classRecords.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                    .count();
            long totalStudents = classRecords.stream()
                    .map(Attendance::getStudentId)
                    .distinct()
                    .count();
            double avgAttendance = classRecords.isEmpty() ? 0.0
                    : Math.round(((double) classPresent / classRecords.size()) * 10000.0) / 100.0;

            Map<String, Object> classMap = new LinkedHashMap<>();
            classMap.put("className", entry.getKey());
            classMap.put("totalStudents", totalStudents);
            classMap.put("avgAttendance", avgAttendance);
            classWiseBreakdown.add(classMap);
        }
        analytics.put("classWiseBreakdown", classWiseBreakdown);

        // 3. Monthly trend
        Map<String, List<Attendance>> byMonth = records.stream()
                .collect(Collectors.groupingBy(a -> {
                    String month = a.getDate().getMonth()
                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    int year = a.getDate().getYear();
                    return month + "|" + year;
                }));
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        byMonth.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().get(0).getDate()))
                .forEach(entry -> {
                    String[] parts = entry.getKey().split("\\|");
                    List<Attendance> monthRecords = entry.getValue();
                    long mPresent = monthRecords.stream()
                            .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                            .count();
                    long mAbsent = monthRecords.stream()
                            .filter(a -> "ABSENT".equals(a.getStatus()))
                            .count();
                    double mPercent = monthRecords.isEmpty() ? 0.0
                            : Math.round(((double) mPresent / monthRecords.size()) * 10000.0) / 100.0;

                    Map<String, Object> monthMap = new LinkedHashMap<>();
                    monthMap.put("month", parts[0]);
                    monthMap.put("year", parts[1]);
                    monthMap.put("presentCount", mPresent);
                    monthMap.put("absentCount", mAbsent);
                    monthMap.put("percentage", mPercent);
                    monthlyTrend.add(monthMap);
                });
        analytics.put("monthlyTrend", monthlyTrend);

        // 4. At-risk students (attendance < 75%)
        analytics.put("atRiskStudents", computeAtRiskStudents(records, 75.0));

        return analytics;
    }

    /**
     * Returns detailed attendance information for a specific student in an academic year.
     */
    public Map<String, Object> getStudentAttendanceDetail(String studentId, String academicYear) {
        List<Attendance> records = attendanceRepository
                .findByStudentIdAndAcademicYear(studentId, academicYear);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No attendance records found for student: " + studentId);
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        long totalDays = records.size();
        long presentDays = records.stream()
                .filter(a -> "PRESENT".equals(a.getStatus())).count();
        long absentDays = records.stream()
                .filter(a -> "ABSENT".equals(a.getStatus())).count();
        long lateDays = records.stream()
                .filter(a -> "LATE".equals(a.getStatus())).count();
        double percentage = totalDays == 0 ? 0.0
                : Math.round(((double) (presentDays + lateDays) / totalDays) * 10000.0) / 100.0;

        detail.put("studentId", studentId);
        detail.put("studentName", records.get(0).getStudentName());
        detail.put("className", records.get(0).getClassName());
        detail.put("totalDays", totalDays);
        detail.put("presentDays", presentDays);
        detail.put("absentDays", absentDays);
        detail.put("lateDays", lateDays);
        detail.put("percentage", percentage);

        // Monthly breakdown
        Map<String, List<Attendance>> byMonth = records.stream()
                .collect(Collectors.groupingBy(a -> {
                    String month = a.getDate().getMonth()
                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    int year = a.getDate().getYear();
                    return month + " " + year;
                }));
        List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
        byMonth.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().get(0).getDate()))
                .forEach(entry -> {
                    List<Attendance> monthRecords = entry.getValue();
                    Map<String, Object> monthMap = new LinkedHashMap<>();
                    monthMap.put("month", entry.getKey());
                    monthMap.put("totalDays", monthRecords.size());
                    monthMap.put("present", monthRecords.stream()
                            .filter(a -> "PRESENT".equals(a.getStatus())).count());
                    monthMap.put("absent", monthRecords.stream()
                            .filter(a -> "ABSENT".equals(a.getStatus())).count());
                    monthMap.put("late", monthRecords.stream()
                            .filter(a -> "LATE".equals(a.getStatus())).count());
                    monthlyBreakdown.add(monthMap);
                });
        detail.put("monthlyBreakdown", monthlyBreakdown);

        // Recent absences (last 10)
        List<Map<String, Object>> recentAbsences = records.stream()
                .filter(a -> "ABSENT".equals(a.getStatus()))
                .sorted(Comparator.comparing(Attendance::getDate).reversed())
                .limit(10)
                .map(a -> {
                    Map<String, Object> absence = new LinkedHashMap<>();
                    absence.put("date", a.getDate().toString());
                    absence.put("remarks", a.getRemarks() != null ? a.getRemarks() : "");
                    return absence;
                })
                .collect(Collectors.toList());
        detail.put("recentAbsences", recentAbsences);

        return detail;
    }

    /**
     * Returns students whose attendance falls below the given threshold percentage.
     */
    public List<Map<String, Object>> getAtRiskStudents(String academicYear, double threshold) {
        List<Attendance> records = attendanceRepository.findByAcademicYear(academicYear);
        return computeAtRiskStudents(records, threshold);
    }

    private List<Map<String, Object>> computeAtRiskStudents(List<Attendance> records, double threshold) {
        Map<String, List<Attendance>> byStudent = records.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        List<Map<String, Object>> atRisk = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : byStudent.entrySet()) {
            List<Attendance> studentRecords = entry.getValue();
            long total = studentRecords.size();
            long present = studentRecords.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                    .count();
            long absent = studentRecords.stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();
            double pct = total == 0 ? 0.0
                    : Math.round(((double) present / total) * 10000.0) / 100.0;

            if (pct < threshold) {
                Map<String, Object> studentMap = new LinkedHashMap<>();
                studentMap.put("studentId", entry.getKey());
                studentMap.put("studentName", studentRecords.get(0).getStudentName());
                studentMap.put("className", studentRecords.get(0).getClassName());
                studentMap.put("attendancePercentage", pct);
                studentMap.put("totalPresent", present);
                studentMap.put("totalAbsent", absent);
                atRisk.add(studentMap);
            }
        }
        // Sort by percentage ascending (worst first)
        atRisk.sort(Comparator.comparingDouble(m -> (double) m.get("attendancePercentage")));
        return atRisk;
    }
}
