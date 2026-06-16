package com.school.manage.controller;

import com.school.manage.dto.auth.AuthResponse;
import com.school.manage.dto.auth.LoginRequest;
import com.school.manage.model.User;
import com.school.manage.service.AuthService;
import com.school.manage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /** Login for SCHOOL_ADMIN, TEACHER, ACCOUNTANT, TRANSPORT_MANAGER, STUDENT, PARENT */
    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> loginTenant(@Valid @RequestBody LoginRequest req) {
        log.info("[AuthController] POST /api/auth/login — email='{}'", req.getEmail());
        return ResponseEntity.ok(authService.loginTenant(req));
    }

    /** Login for SUPER_ADMIN (platform-level) */
    @PostMapping("/platform/auth/login")
    public ResponseEntity<AuthResponse> loginPlatform(@Valid @RequestBody LoginRequest req) {
        log.info("[AuthController] POST /platform/auth/login — email='{}'", req.getEmail());
        return ResponseEntity.ok(authService.loginPlatform(req));
    }

    /** Exchange a valid refresh token for a new access + refresh token pair */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        log.debug("[AuthController] POST /api/auth/refresh");
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("[AuthController] Refresh request missing refreshToken field.");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    /** Logout acknowledgement — actual token invalidation is client-side */
    @PostMapping("/api/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("[AuthController] POST /api/auth/logout");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Set initial password — first-login flow.
     *
     * Only allowed when the authenticated user's {@code mustChangePassword} flag
     * is true.  Does NOT require the current (default) password.
     * Requires a valid Bearer JWT token.
     */
    @PostMapping("/api/auth/set-initial-password")
    public ResponseEntity<Map<String, String>> setInitialPassword(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            log.warn("[AuthController] POST /api/auth/set-initial-password — no authentication");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        User principal = (User) auth.getPrincipal();
        String newPassword = body.get("newPassword");
        log.info("[AuthController] POST /api/auth/set-initial-password — userId='{}'", principal.getId());
        userService.setInitialPassword(principal.getId(), newPassword);
        return ResponseEntity.ok(Map.of("message", "Password set successfully"));
    }
}
