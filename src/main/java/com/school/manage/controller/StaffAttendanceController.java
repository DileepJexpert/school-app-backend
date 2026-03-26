package com.school.manage.controller;

import com.school.manage.model.StaffAttendance;
import com.school.manage.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/staff-attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> markAttendance(
            @RequestBody List<StaffAttendance> entries) {
        log.info("[StaffAttendanceController] POST /api/staff-attendance/mark count={}", entries.size());
        return new ResponseEntity<>(staffAttendanceService.markBulkAttendance(entries), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(staffAttendanceService.getAttendanceByDate(date));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByStaff(
            @PathVariable String staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(staffAttendanceService.getAttendanceByStaff(staffId, from, to));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByDepartment(
            @PathVariable String department,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(staffAttendanceService.getAttendanceByDepartment(department, date));
    }
}
