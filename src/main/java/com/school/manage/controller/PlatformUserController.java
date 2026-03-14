package com.school.manage.controller;

import com.school.manage.dto.auth.CreateUserRequest;
import com.school.manage.dto.auth.UserDto;
import com.school.manage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Platform-level user management (SUPER_ADMIN only).
 * Operates on platform_db — not subject to tenant interceptor.
 */
@Slf4j
@RestController
@RequestMapping("/platform/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlatformUserController {

    private final UserService userService;

    /** Bootstrap or add a new SUPER_ADMIN (existing SUPER_ADMIN required after first run) */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> createSuperAdmin(@Valid @RequestBody CreateUserRequest req) {
        log.info("[PlatformUserController] POST /platform/users — creating SUPER_ADMIN: email='{}'", req.getEmail());
        return new ResponseEntity<>(userService.createSuperAdmin(req), HttpStatus.CREATED);
    }
}
