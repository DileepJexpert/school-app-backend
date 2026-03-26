package com.school.manage.controller;

import com.school.manage.model.Incident;
import com.school.manage.dto.DisciplineSummaryDto;
import com.school.manage.service.DisciplineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/discipline")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DisciplineController {

    private final DisciplineService disciplineService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Incident> createIncident(@RequestBody Incident incident) {
        log.info("Creating incident for student: {}", incident.getStudentId());
        return ResponseEntity.ok(disciplineService.createIncident(incident));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Incident>> getAllIncidents(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String severity) {
        if (className != null) {
            return ResponseEntity.ok(disciplineService.getByClassName(className));
        }
        if (severity != null) {
            return ResponseEntity.ok(disciplineService.getBySeverity(severity));
        }
        return ResponseEntity.ok(disciplineService.getAllIncidents());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT')")
    public ResponseEntity<List<Incident>> getStudentIncidents(@PathVariable String studentId) {
        return ResponseEntity.ok(disciplineService.getByStudentId(studentId));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Incident> resolveIncident(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body) {
        String resolution = body.getOrDefault("resolution", "");
        return ResponseEntity.ok(disciplineService.resolveIncident(id, resolution));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<DisciplineSummaryDto> getSummary() {
        return ResponseEntity.ok(disciplineService.getSummary());
    }
}
