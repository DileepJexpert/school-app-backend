package com.school.manage.controller;

import com.school.manage.dto.AttendanceRequestDto;
import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.model.Attendance;
import com.school.manage.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Mark attendance for one or more students in a class.
     * Accepts a bulk request with a list of student attendance entries.
     *
     * POST /api/attendance/mark
     */
    @PostMapping("/mark")
    public ResponseEntity<List<Attendance>> markAttendance(
            @RequestBody AttendanceRequestDto request) {
        List<Attendance> saved = attendanceService.markBulkAttendance(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get attendance records for a specific student within a date range.
     *
     * GET /api/attendance/student/{studentId}?from=2024-06-01&to=2024-06-30
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>> getStudentAttendance(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByStudentAndDateRange(studentId, from, to));
    }

    /**
     * Get all attendance records for a class on a specific date.
     *
     * GET /api/attendance/class/{className}?date=2024-06-01
     */
    @GetMapping("/class/{className}")
    public ResponseEntity<List<Attendance>> getClassAttendance(
            @PathVariable String className,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByClassAndDate(className, date));
    }

    /**
     * Get attendance records for a class within a date range.
     *
     * GET /api/attendance/class/{className}/range?from=2024-06-01&to=2024-06-30&academicYear=2024-25
     */
    @GetMapping("/class/{className}/range")
    public ResponseEntity<List<Attendance>> getClassAttendanceRange(
            @PathVariable String className,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByClassAndDateRange(className, academicYear, from, to));
    }

    /**
     * Get attendance summary for a student in an academic year.
     *
     * GET /api/attendance/student/{studentId}/summary?academicYear=2024-25
     */
    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<AttendanceSummaryDto> getAttendanceSummary(
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceSummary(studentId, academicYear));
    }

    /**
     * Delete an attendance record by its ID.
     *
     * DELETE /api/attendance/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable String id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
