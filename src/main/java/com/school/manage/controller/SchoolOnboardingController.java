package com.school.manage.controller;

import com.school.manage.model.School;
import com.school.manage.service.SchoolOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Platform-level REST API for managing schools.
 * Base path: /platform/schools
 *
 * These endpoints are NOT tenant-scoped — they operate on platform_db.
 * In production, protect these with an admin secret header or role-based auth.
 */
@RestController
@RequestMapping("/platform/schools")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolOnboardingController {

    private final SchoolOnboardingService onboardingService;

    /**
     * Register a new school.
     * POST /platform/schools
     * Body: { "tenantId": "springfield", "name": "Springfield International", "adminEmail": "...", ... }
     */
    @PostMapping
    public ResponseEntity<?> registerSchool(@RequestBody School school) {
        try {
            School registered = onboardingService.registerSchool(school);
            return new ResponseEntity<>(registered, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all registered schools.
     * GET /platform/schools
     */
    @GetMapping
    public ResponseEntity<List<School>> getAllSchools() {
        return ResponseEntity.ok(onboardingService.getAllSchools());
    }

    /**
     * Get a school by tenantId.
     * GET /platform/schools/{tenantId}
     */
    @GetMapping("/{tenantId}")
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
