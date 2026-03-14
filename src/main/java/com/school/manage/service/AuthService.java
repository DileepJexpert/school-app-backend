package com.school.manage.service;

import com.school.manage.config.JwtService;
import com.school.manage.dto.auth.AuthResponse;
import com.school.manage.dto.auth.LoginRequest;
import com.school.manage.model.User;
import com.school.manage.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;           // tenant-aware (primary)
    private final MongoTemplate platformMongoTemplate;   // always platform_db

    public AuthService(JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       MongoTemplate mongoTemplate,
                       @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    // ── Login ────────────────────────────────────────────────────────────────

    /**
     * Login for school-level users (SCHOOL_ADMIN, TEACHER, etc.).
     * Looks up the user in the tenant DB set by TenantContext.
     */
    public AuthResponse loginTenant(LoginRequest req) {
        User user = findInTenantDb(req.getEmail());
        return authenticate(user, req.getPassword(), mongoTemplate);
    }

    /**
     * Login for SUPER_ADMIN users.
     * Always looks up in platform_db regardless of tenant context.
     */
    public AuthResponse loginPlatform(LoginRequest req) {
        User user = findInPlatformDb(req.getEmail());
        return authenticate(user, req.getPassword(), platformMongoTemplate);
    }

    // ── Token refresh ────────────────────────────────────────────────────────

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        String userId   = jwtService.extractUserId(refreshToken);
        String tenantId = jwtService.extractTenantId(refreshToken);

        User user = findUserById(userId, tenantId);
        if (user == null || !user.isActive()) {
            throw new RuntimeException("User not found or inactive");
        }

        String newToken        = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return buildResponse(user, newToken, newRefreshToken);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse authenticate(User user, String rawPassword, MongoTemplate template) {
        if (user == null || !user.isActive()) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLoginAt(LocalDateTime.now());
        template.save(user);

        String token        = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return buildResponse(user, token, refreshToken);
    }

    private AuthResponse buildResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .linkedEntityId(user.getLinkedEntityId())
                .permissions(effectivePermissions(user))
                .expiresIn(jwtService.getJwtExpiration())
                .build();
    }

    private User findInTenantDb(String email) {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("email").is(email)), User.class);
    }

    private User findInPlatformDb(String email) {
        return platformMongoTemplate.findOne(
                Query.query(Criteria.where("email").is(email)), User.class);
    }

    private User findUserById(String userId, String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return platformMongoTemplate.findById(userId, User.class);
        }
        // Temporarily switch context to look up user in correct DB
        String previousTenant = TenantContext.getTenant();
        try {
            TenantContext.setTenant(tenantId);
            return mongoTemplate.findById(userId, User.class);
        } finally {
            if (previousTenant != null) TenantContext.setTenant(previousTenant);
            else TenantContext.clear();
        }
    }

    /**
     * Returns the union of role-default permissions and any extra permissions
     * assigned to the user.
     *
     * Permission format: "resource:action[:scope]"
     *   Examples: "students:read", "fees:write", "results:read:own"
     */
    public List<String> effectivePermissions(User user) {
        List<String> perms = new ArrayList<>(roleDefaults(user.getRole()));
        if (user.getExtraPermissions() != null) {
            perms.addAll(user.getExtraPermissions());
        }
        return perms;
    }

    private List<String> roleDefaults(com.school.manage.enums.UserRole role) {
        return switch (role) {
            case SUPER_ADMIN -> List.of("*");  // unrestricted
            case SCHOOL_ADMIN -> List.of(
                    "students:read",       "students:write",
                    "attendance:read",     "attendance:write",
                    "fees:read",           "fees:write",
                    "results:read",        "results:write",
                    "transport:read",      "transport:write",
                    "expenses:read",       "expenses:write",
                    "notifications:read",  "notifications:write",
                    "timetable:read",      "timetable:write",
                    "reports:read",        "users:read", "users:write"
            );
            case TEACHER -> List.of(
                    "students:read",
                    "attendance:read",  "attendance:write",
                    "results:read",     "results:write",
                    "timetable:read",   "timetable:write",
                    "notifications:read"
            );
            case ACCOUNTANT -> List.of(
                    "students:read",
                    "fees:read",        "fees:write",
                    "expenses:read",    "expenses:write",
                    "reports:read"
            );
            case TRANSPORT_MANAGER -> List.of(
                    "students:read",
                    "transport:read",   "transport:write"
            );
            case STUDENT -> List.of(
                    "attendance:read:own",
                    "fees:read:own",
                    "results:read:own",
                    "timetable:read",
                    "notifications:read:own"
            );
            case PARENT -> List.of(
                    "attendance:read:child",
                    "fees:read:child",
                    "results:read:child",
                    "timetable:read",
                    "notifications:read:child"
            );
        };
    }
}
