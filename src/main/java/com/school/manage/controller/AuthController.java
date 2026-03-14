package com.school.manage.controller;

import com.school.manage.dto.auth.AuthResponse;
import com.school.manage.dto.auth.LoginRequest;
import com.school.manage.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication endpoints.
 *
 *  POST /api/auth/login          – school-level users (uses X-Tenant-ID header)
 *  POST /platform/auth/login     – SUPER_ADMIN login (uses platform_db)
 *  POST /api/auth/refresh        – exchange refresh token for new access token
 *  POST /api/auth/logout         – client-side logout acknowledgement
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /** Login for SCHOOL_ADMIN, TEACHER, ACCOUNTANT, TRANSPORT_MANAGER, STUDENT, PARENT */
    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> loginTenant(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.loginTenant(req));
    }

    /** Login for SUPER_ADMIN (platform-level) */
    @PostMapping("/platform/auth/login")
    public ResponseEntity<AuthResponse> loginPlatform(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.loginPlatform(req));
    }

    /** Exchange a valid refresh token for a new access + refresh token pair */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    /** Logout acknowledgement — actual token invalidation is client-side */
    @PostMapping("/api/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
