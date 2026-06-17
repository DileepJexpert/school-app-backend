package com.school.manage.controller;

import com.school.manage.model.HealthRecord;
import com.school.manage.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<HealthRecord>> getAll() {
        log.info("[HealthRecordController] GET /api/health-records");
        return ResponseEntity.ok(healthRecordService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<HealthRecord> getById(@PathVariable String id) {
        log.info("[HealthRecordController] GET /api/health-records/{}", id);
        return ResponseEntity.ok(healthRecordService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<HealthRecord> getByStudent(@PathVariable String studentId) {
        log.info("[HealthRecordController] GET /api/health-records/student/{}", studentId);
        return ResponseEntity.ok(healthRecordService.getByStudentId(studentId)
                .orElse(null));
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<HealthRecord>> getByClass(@PathVariable String className) {
        log.info("[HealthRecordController] GET /api/health-records/class/{}", className);
        return ResponseEntity.ok(healthRecordService.getByClass(className));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<HealthRecord> createOrUpdate(@RequestBody HealthRecord record) {
        log.info("[HealthRecordController] POST /api/health-records — studentId='{}'", record.getStudentId());
        return new ResponseEntity<>(healthRecordService.createOrUpdate(record), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[HealthRecordController] DELETE /api/health-records/{}", id);
        healthRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/student/{studentId}/visit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<HealthRecord> addMedicalVisit(
            @PathVariable String studentId,
            @RequestBody HealthRecord.MedicalVisit visit) {
        log.info("[HealthRecordController] POST /api/health-records/student/{}/visit", studentId);
        return ResponseEntity.ok(healthRecordService.addMedicalVisit(studentId, visit));
    }

    @DeleteMapping("/student/{studentId}/visit/{visitId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<HealthRecord> removeMedicalVisit(
            @PathVariable String studentId,
            @PathVariable String visitId) {
        log.info("[HealthRecordController] DELETE /api/health-records/student/{}/visit/{}", studentId, visitId);
        return ResponseEntity.ok(healthRecordService.removeMedicalVisit(studentId, visitId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[HealthRecordController] GET /api/health-records/stats");
        return ResponseEntity.ok(healthRecordService.getStats());
    }
}
