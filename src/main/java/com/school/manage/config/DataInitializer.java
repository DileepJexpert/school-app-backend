package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds essential platform users on first startup.
 *
 * Default credentials (CHANGE these immediately in production via the /api/users/change-password endpoint):
 *
 *   SUPER_ADMIN  → email: superadmin@platform.com   password: SuperAdmin@123
 *
 * For each school tenant, SCHOOL_ADMIN users should be created via:
 *   POST /api/users  (with SUPER_ADMIN or SCHOOL_ADMIN JWT)
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    // Always uses platform_db
    private final MongoTemplate platformMongoTemplate;

    public DataInitializer(PasswordEncoder passwordEncoder,
                           @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("[DataInitializer] Running startup data seeding...");
        seedSuperAdmin();
        log.info("[DataInitializer] Startup data seeding complete.");
    }

    private void seedSuperAdmin() {
        String email = "superadmin@platform.com";
        log.debug("[DataInitializer] Checking if SUPER_ADMIN exists in platform_db for email='{}'", email);

        boolean exists = platformMongoTemplate.exists(
                Query.query(Criteria.where("email").is(email)), User.class);

        if (exists) {
            log.info("[DataInitializer] SUPER_ADMIN already exists — skipping seed.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setEmail(email);
        superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
        superAdmin.setFullName("Platform Super Admin");
        superAdmin.setRole(UserRole.SUPER_ADMIN);
        superAdmin.setTenantId(null); // Platform-level, no tenant
        superAdmin.setActive(true);

        platformMongoTemplate.save(superAdmin);

        log.warn("=================================================================");
        log.warn("  DEFAULT SUPER_ADMIN CREATED — CHANGE PASSWORD IMMEDIATELY!");
        log.warn("  Email   : superadmin@platform.com");
        log.warn("  Password: SuperAdmin@123");
        log.warn("  Login at: POST /platform/auth/login");
        log.warn("=================================================================");
    }
}
