package com.school.manage.controller;

import com.school.manage.dto.auth.ChangePasswordRequest;
import com.school.manage.dto.auth.CreateUserRequest;
import com.school.manage.dto.auth.UserDto;
import com.school.manage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return new ResponseEntity<>(userService.createTenantUser(req), HttpStatus.CREATED);
    }

    /** List all users in the current tenant */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userService.listTenantUsers());
    }

    /** Get a single user by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getTenantUser(id));
    }

    /** Update user details (role, name, phone, extra permissions) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id,
                                              @Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.updateTenantUser(id, req));
    }

    /** Deactivate (soft-delete) a user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Change own password — any authenticated user */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        userService.changePassword(auth.getName(), req);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
