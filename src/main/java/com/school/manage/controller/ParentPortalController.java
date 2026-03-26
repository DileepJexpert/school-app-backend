package com.school.manage.controller;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ParentDashboardDto;
import com.school.manage.model.Attendance;
import com.school.manage.model.User;
import com.school.manage.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;
    private final FeeService feeService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDashboardDto> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] GET /api/parent/dashboard");
        return ResponseEntity.ok(parentPortalService.getDashboard(user));
    }

    @GetMapping("/child/{studentId}/attendance")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Attendance>> getChildAttendance(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(attendanceService.getAttendanceByStudentAndDateRange(studentId, from, to));
    }

    @GetMapping("/child/{studentId}/attendance/summary")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<AttendanceSummaryDto> getChildAttendanceSummary(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(studentId, academicYear));
    }

    @GetMapping("/child/{studentId}/results")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getChildResults(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(resultService.getStudentReportCard(studentId, academicYear));
    }

    @GetMapping("/child/{studentId}/fees")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getChildFees(
            Authentication auth,
            @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(feeService.getStudentFeeProfile(studentId));
    }
}
