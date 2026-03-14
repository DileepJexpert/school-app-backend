package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.model.User;
import lombok.RequiredArgsConstructor;
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
@Component
@RequiredArgsConstructor
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
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        String email = "superadmin@platform.com";
        boolean exists = platformMongoTemplate.exists(
                Query.query(Criteria.where("email").is(email)), User.class);

        if (!exists) {
            User superAdmin = new User();
            superAdmin.setEmail(email);
            superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
            superAdmin.setFullName("Platform Super Admin");
            superAdmin.setRole(UserRole.SUPER_ADMIN);
            superAdmin.setTenantId(null); // Platform-level, no tenant
            superAdmin.setActive(true);

            platformMongoTemplate.save(superAdmin);
            System.out.println("=================================================================");
            System.out.println("  DEFAULT SUPER_ADMIN CREATED — CHANGE PASSWORD IMMEDIATELY!");
            System.out.println("  Email   : superadmin@platform.com");
            System.out.println("  Password: SuperAdmin@123");
            System.out.println("  Login at: POST /platform/auth/login");
            System.out.println("=================================================================");
        }
    }
}
