package com.school.manage.controller;

import com.school.manage.model.School;
import com.school.manage.service.SchoolOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/platform/schools")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolOnboardingController {

    private final SchoolOnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerSchool(@RequestBody School school) {
        try {
            School registered = onboardingService.registerSchool(school);
            return new ResponseEntity<>(registered, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<School>> getAllSchools() {
        return ResponseEntity.ok(onboardingService.getAllSchools());
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSchool(@PathVariable String tenantId) {
        School school = onboardingService.getByTenantId(tenantId);
        if (school == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(school);
    }

    /**
     * Check if a tenantId is valid and active (used by Flutter login screen).
     * GET /platform/schools/{tenantId}/validate
     * Response: { "valid": true, "name": "Springfield International Academy" }
     */
    @GetMapping("/{tenantId}/validate")
    public ResponseEntity<Map<String, Object>> validateTenant(@PathVariable String tenantId) {
        School school = onboardingService.getByTenantId(tenantId);
        if (school == null || !school.isActive()) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "name", school.getName(),
                "city", school.getCity() != null ? school.getCity() : "",
                "board", school.getBoard() != null ? school.getBoard() : ""
        ));
    }

    /**
     * Activate or deactivate a school.
     * PUT /platform/schools/{tenantId}/status
     * Body: { "active": true }
     */
    @PutMapping("/{tenantId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable String tenantId,
            @RequestBody Map<String, Boolean> body) {
        try {
            boolean active = Boolean.TRUE.equals(body.get("active"));
            School updated = onboardingService.setActive(tenantId, active);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
