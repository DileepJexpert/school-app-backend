package com.school.manage.service;

import com.school.manage.dto.auth.ChangePasswordRequest;
import com.school.manage.dto.auth.CreateUserRequest;
import com.school.manage.dto.auth.UserDto;
import com.school.manage.enums.UserRole;
import com.school.manage.model.User;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate platformMongoTemplate;

    public UserService(PasswordEncoder passwordEncoder,
                       MongoTemplate mongoTemplate,
                       @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    // ── Create ───────────────────────────────────────────────────────────────

    /**
     * Creates a school-level user in the current tenant DB.
     * Called by SCHOOL_ADMIN.
     */
    public UserDto createTenantUser(CreateUserRequest req) {
        String tenantId = TenantContext.getTenant();
        log.info("[UserService] Creating tenant user: email='{}', role='{}', tenant='{}'",
                req.getEmail(), req.getRole(), tenantId);
        return createUser(req, tenantId, mongoTemplate);
    }

    /**
     * Creates a SUPER_ADMIN user in platform_db.
     * Called only during bootstrap or by an existing SUPER_ADMIN.
     */
    public UserDto createSuperAdmin(CreateUserRequest req) {
        log.info("[UserService] Creating SUPER_ADMIN: email='{}'", req.getEmail());
        req = cloneWithRole(req, UserRole.SUPER_ADMIN);
        return createUser(req, null, platformMongoTemplate);
    }

    private UserDto createUser(CreateUserRequest req, String tenantId, MongoTemplate template) {
        // Check duplicate email in the target DB
        boolean exists = template.exists(
                Query.query(Criteria.where("email").is(req.getEmail())), User.class);
        if (exists) {
            log.warn("[UserService] User creation FAILED — email already in use: '{}'", req.getEmail());
            throw new RuntimeException("Email already in use: " + req.getEmail());
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setTenantId(tenantId);
        user.setLinkedEntityId(req.getLinkedEntityId());
        user.setExtraPermissions(req.getExtraPermissions());
        user.setActive(true);

        template.save(user);
        log.info("[UserService] User created: id='{}', email='{}', role='{}', tenant='{}'",
                user.getId(), user.getEmail(), user.getRole(), tenantId);
        return toDto(user);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public List<UserDto> listTenantUsers() {
        log.debug("[UserService] Listing all users for tenant='{}'", TenantContext.getTenant());
        return mongoTemplate.findAll(User.class).stream().map(this::toDto).toList();
    }

    public UserDto getTenantUser(String userId) {
        log.debug("[UserService] Fetching user: userId='{}'", userId);
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            log.warn("[UserService] User not found: userId='{}'", userId);
            throw new RuntimeException("User not found: " + userId);
        }
        return toDto(user);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    public UserDto updateTenantUser(String userId, CreateUserRequest req) {
        log.info("[UserService] Updating user: userId='{}', newRole='{}'", userId, req.getRole());
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            log.warn("[UserService] Update FAILED — user not found: userId='{}'", userId);
            throw new RuntimeException("User not found: " + userId);
        }

        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setLinkedEntityId(req.getLinkedEntityId());
        user.setExtraPermissions(req.getExtraPermissions());

        // Only update password if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            log.debug("[UserService] Password updated for userId='{}'", userId);
        }

        mongoTemplate.save(user);
        log.info("[UserService] User updated successfully: userId='{}'", userId);
        return toDto(user);
    }

    public void deactivateUser(String userId) {
        log.info("[UserService] Deactivating user: userId='{}'", userId);
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            log.warn("[UserService] Deactivate FAILED — user not found: userId='{}'", userId);
            throw new RuntimeException("User not found: " + userId);
        }
        user.setActive(false);
        mongoTemplate.save(user);
        log.info("[UserService] User deactivated: userId='{}', email='{}'", userId, user.getEmail());
    }

    public void changePassword(String userId, ChangePasswordRequest req) {
        log.info("[UserService] Password change request for userId='{}'", userId);
        // Try tenant DB first, then platform DB
        User user = mongoTemplate.findById(userId, User.class);
        MongoTemplate template = mongoTemplate;

        if (user == null) {
            log.debug("[UserService] User not in tenant DB, checking platform_db for userId='{}'", userId);
            user = platformMongoTemplate.findById(userId, User.class);
            template = platformMongoTemplate;
        }
        if (user == null) {
            log.warn("[UserService] Password change FAILED — user not found: userId='{}'", userId);
            throw new RuntimeException("User not found: " + userId);
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            log.warn("[UserService] Password change FAILED — current password mismatch for userId='{}'", userId);
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        template.save(user);
        log.info("[UserService] Password changed successfully for userId='{}'", userId);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setTenantId(user.getTenantId());
        dto.setLinkedEntityId(user.getLinkedEntityId());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setExtraPermissions(user.getExtraPermissions());
        return dto;
    }

    private CreateUserRequest cloneWithRole(CreateUserRequest req, UserRole role) {
        CreateUserRequest copy = new CreateUserRequest();
        copy.setEmail(req.getEmail());
        copy.setPassword(req.getPassword());
        copy.setFullName(req.getFullName());
        copy.setPhone(req.getPhone());
        copy.setRole(role);
        copy.setLinkedEntityId(req.getLinkedEntityId());
        copy.setExtraPermissions(req.getExtraPermissions());
        return copy;
    }
}
