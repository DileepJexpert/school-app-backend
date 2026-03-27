package com.school.manage.controller;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ChildOverviewDto;
import com.school.manage.model.Attendance;
import com.school.manage.model.Homework;
import com.school.manage.model.User;
import com.school.manage.service.FeeService;
import com.school.manage.service.HomeworkService;
import com.school.manage.service.ResultService;
import com.school.manage.service.StudentPortalService;
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
@RequestMapping("/api/student-portal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;
    private final ResultService resultService;
    private final FeeService feeService;
    private final HomeworkService homeworkService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChildOverviewDto> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/dashboard");
        return ResponseEntity.ok(studentPortalService.getDashboard(user.getLinkedEntityId()));
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Attendance>> getMyAttendance(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(studentPortalService.getMyAttendance(user.getLinkedEntityId(), from, to));
    }

    @GetMapping("/attendance/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttendanceSummaryDto> getMyAttendanceSummary(
            Authentication auth,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(studentPortalService.getMyAttendanceSummary(user.getLinkedEntityId(), academicYear));
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyResults(
            Authentication auth,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(resultService.getStudentReportCard(user.getLinkedEntityId(), academicYear));
    }

    @GetMapping("/fees")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyFees(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(feeService.getStudentFeeProfile(user.getLinkedEntityId()));
    }

    @GetMapping("/homework")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Homework>> getMyHomework(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[StudentPortalController] GET /api/student-portal/homework for user={}", user.getId());
        return ResponseEntity.ok(homeworkService.getHomeworkForStudent(user.getLinkedEntityId()));
    }
}
