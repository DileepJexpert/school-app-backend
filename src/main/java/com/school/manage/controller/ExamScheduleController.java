package com.school.manage.controller;

import com.school.manage.model.ExamSchedule;
import com.school.manage.service.ExamScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exam-schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<ExamSchedule> createSchedule(@RequestBody ExamSchedule schedule) {
        log.info("[ExamScheduleController] POST /api/exam-schedules — examName='{}', class='{}'",
                schedule.getExamName(), schedule.getClassName());
        return new ResponseEntity<>(examScheduleService.createSchedule(schedule), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExamSchedule>> getAllSchedules(@RequestParam String academicYear) {
        log.info("[ExamScheduleController] GET /api/exam-schedules — academicYear='{}'", academicYear);
        return ResponseEntity.ok(examScheduleService.getAllSchedules(academicYear));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamSchedule> getScheduleById(@PathVariable String id) {
        log.info("[ExamScheduleController] GET /api/exam-schedules/{}", id);
        return ResponseEntity.ok(examScheduleService.getScheduleById(id));
    }

    @GetMapping("/class/{className}")
    public ResponseEntity<List<ExamSchedule>> getSchedulesByClass(
            @PathVariable String className,
            @RequestParam String academicYear,
            @RequestParam(required = false) String status) {
        log.info("[ExamScheduleController] GET /api/exam-schedules/class/{} — academicYear='{}', status='{}'",
                className, academicYear, status);
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(examScheduleService.getPublishedSchedules(className, academicYear));
        }
        return ResponseEntity.ok(examScheduleService.getSchedulesByClass(className, academicYear));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<ExamSchedule> updateSchedule(@PathVariable String id,
                                                       @RequestBody ExamSchedule schedule) {
        log.info("[ExamScheduleController] PUT /api/exam-schedules/{}", id);
        return ResponseEntity.ok(examScheduleService.updateSchedule(id, schedule));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<ExamSchedule> publishSchedule(@PathVariable String id) {
        log.info("[ExamScheduleController] PUT /api/exam-schedules/{}/publish", id);
        return ResponseEntity.ok(examScheduleService.publishSchedule(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
        log.info("[ExamScheduleController] DELETE /api/exam-schedules/{}", id);
        examScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
