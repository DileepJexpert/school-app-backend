package com.school.manage.dto.auth;

import com.school.manage.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;

    private String userId;
    private String email;
    private String fullName;
    private UserRole role;

    /** School tenantId (null for SUPER_ADMIN) */
    private String tenantId;

    /** Linked Student/Teacher/Parent ID (null for admin roles) */
    private String linkedEntityId;

    /** Effective permissions for this user (role defaults + extras) */
    private List<String> permissions;

    /** Token validity in seconds */
    private long expiresIn;
}
