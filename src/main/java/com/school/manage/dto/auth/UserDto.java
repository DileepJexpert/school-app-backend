package com.school.manage.dto.auth;

import com.school.manage.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Safe user representation — never includes password. */
@Data
public class UserDto {

    private String id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private String tenantId;
    private String linkedEntityId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private List<String> extraPermissions;
}
