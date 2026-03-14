package com.school.manage.model;

import com.school.manage.enums.UserRole;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Platform user entity.
 *
 * Storage location:
 *   - SUPER_ADMIN users  → platform_db.users
 *   - All other roles    → {tenantId}_db.users
 *
 * linkedEntityId maps to a domain entity for scoped data access:
 *   - TEACHER            → Teacher.id  (future)
 *   - STUDENT            → Student.id
 *   - PARENT             → Parent.id   (future)
 *   - Others             → null
 */
@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    /** BCrypt-hashed password. Never returned in API responses. */
    private String password;

    private String fullName;

    private String phone;

    private UserRole role;

    /**
     * Fine-grained permissions beyond the role defaults.
     * Format: "resource:action" e.g. "reports:read", "fees:write"
     */
    private List<String> extraPermissions;

    /** School tenantId this user belongs to. Null for SUPER_ADMIN. */
    private String tenantId;

    /**
     * ID of the linked domain entity (Student, Teacher, Parent).
     * Used to scope data access for STUDENT and PARENT roles.
     */
    private String linkedEntityId;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastLoginAt;
}
