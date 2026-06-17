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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/staff-attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<StaffAttendance> markAttendance(@RequestBody StaffAttendance attendance) {
        log.info("[StaffAttendanceController] POST /api/staff-attendance — userId='{}'", attendance.getUserId());
        return new ResponseEntity<>(staffAttendanceService.markAttendance(attendance), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> markBulkAttendance(@RequestBody List<StaffAttendance> attendanceList) {
        log.info("[StaffAttendanceController] POST /api/staff-attendance/bulk — count={}", attendanceList.size());
        return new ResponseEntity<>(staffAttendanceService.markBulkAttendance(attendanceList), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<StaffAttendance> updateAttendance(
            @PathVariable String id,
            @RequestBody StaffAttendance attendance) {
        log.info("[StaffAttendanceController] PUT /api/staff-attendance/{}", id);
        return ResponseEntity.ok(staffAttendanceService.updateAttendance(id, attendance));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(staffAttendanceService.getByDate(date));
    }

    @GetMapping("/staff/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByStaff(@PathVariable String userId) {
        return ResponseEntity.ok(staffAttendanceService.getByStaff(userId));
    }

    @GetMapping("/staff/{userId}/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByStaffAndRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(staffAttendanceService.getByStaffAndDateRange(userId, from, to));
    }

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<StaffAttendance>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(staffAttendanceService.getByDateRange(from, to));
    }

    @GetMapping("/today-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getTodayStats() {
        return ResponseEntity.ok(staffAttendanceService.getTodayStats());
    }
}
