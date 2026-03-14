package com.school.manage.controller;

import com.school.manage.dto.auth.ChangePasswordRequest;
import com.school.manage.dto.auth.CreateUserRequest;
import com.school.manage.dto.auth.UserDto;
import com.school.manage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User management endpoints (school-level users only).
 *
 * SCHOOL_ADMIN can create and manage users within their own tenant.
 * SUPER_ADMIN platform user management is done via /platform/users.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /** Create a new user in the current tenant (SCHOOL_ADMIN only) */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        log.info("[UserController] POST /api/users — email='{}', role='{}'", req.getEmail(), req.getRole());
        return new ResponseEntity<>(userService.createTenantUser(req), HttpStatus.CREATED);
    }

    /** List all users in the current tenant */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<List<UserDto>> listUsers() {
        log.debug("[UserController] GET /api/users");
        return ResponseEntity.ok(userService.listTenantUsers());
    }

    /** Get a single user by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        log.debug("[UserController] GET /api/users/{}", id);
        return ResponseEntity.ok(userService.getTenantUser(id));
    }

    /** Update user details (role, name, phone, extra permissions) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id,
                                              @Valid @RequestBody CreateUserRequest req) {
        log.info("[UserController] PUT /api/users/{} — newRole='{}'", id, req.getRole());
        return ResponseEntity.ok(userService.updateTenantUser(id, req));
    }

    /** Deactivate (soft-delete) a user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        log.info("[UserController] DELETE /api/users/{} (deactivate)", id);
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Change own password — any authenticated user */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        log.info("[UserController] POST /api/users/change-password — userId='{}'", auth.getName());
        userService.changePassword(auth.getName(), req);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
