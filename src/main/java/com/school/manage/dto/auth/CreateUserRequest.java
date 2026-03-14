package com.school.manage.dto.auth;

import com.school.manage.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;

    /**
     * For STUDENT role: set this to the Student.id
     * For TEACHER role: set this to the Teacher.id (future)
     * For PARENT role:  set this to the Parent.id  (future)
     */
    private String linkedEntityId;

    /** Additional fine-grained permissions beyond role defaults */
    private List<String> extraPermissions;
}
