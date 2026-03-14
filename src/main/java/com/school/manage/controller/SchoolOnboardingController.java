package com.school.manage.controller;

import com.school.manage.model.School;
import com.school.manage.service.SchoolOnboardingService;
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
@RequestMapping("/platform/schools")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SchoolOnboardingController {

    private final SchoolOnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerSchool(@RequestBody School school) {
        log.info("[SchoolOnboarding] POST /platform/schools — tenantId='{}', name='{}'",
                school.getTenantId(), school.getName());
        try {
            School registered = onboardingService.registerSchool(school);
            log.info("[SchoolOnboarding] School registered: tenantId='{}', id='{}'",
                    registered.getTenantId(), registered.getId());
            return new ResponseEntity<>(registered, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("[SchoolOnboarding] Registration FAILED for tenantId='{}': {}",
                    school.getTenantId(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<School>> getAllSchools() {
        log.debug("[SchoolOnboarding] GET /platform/schools");
        return ResponseEntity.ok(onboardingService.getAllSchools());
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSchool(@PathVariable String tenantId) {
        log.debug("[SchoolOnboarding] GET /platform/schools/{}", tenantId);
        School school = onboardingService.getByTenantId(tenantId);
        if (school == null) {
            log.warn("[SchoolOnboarding] School not found: tenantId='{}'", tenantId);
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
        log.debug("[SchoolOnboarding] GET /platform/schools/{}/validate", tenantId);
        School school = onboardingService.getByTenantId(tenantId);
        if (school == null || !school.isActive()) {
            log.info("[SchoolOnboarding] Tenant validation FAILED: tenantId='{}'", tenantId);
            return ResponseEntity.ok(Map.of("valid", false));
        }
        log.info("[SchoolOnboarding] Tenant validated OK: tenantId='{}', name='{}'", tenantId, school.getName());
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
        boolean active = Boolean.TRUE.equals(body.get("active"));
        log.info("[SchoolOnboarding] PUT /platform/schools/{}/status — active={}", tenantId, active);
        try {
            School updated = onboardingService.setActive(tenantId, active);
            log.info("[SchoolOnboarding] School '{}' active status → {}", tenantId, active);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("[SchoolOnboarding] Status update FAILED for tenantId='{}': {}", tenantId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
