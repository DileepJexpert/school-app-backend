package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.model.AiConfig;
import com.school.manage.model.School;
import com.school.manage.model.Student;
import com.school.manage.model.User;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds essential platform users and test data on first startup.
 *
 * Default credentials (CHANGE these immediately in production via the /api/users/change-password endpoint):
 *
 *   SUPER_ADMIN  → email: superadmin@platform.com   password: SuperAdmin@123
 *
 * Test school "demo" credentials (for development/testing only):
 *
 *   School code : demo
 *   SCHOOL_ADMIN → email: admin@demo.com       password: Admin@123
 *   TEACHER      → email: teacher@demo.com     password: Teacher@123
 *   STUDENT      → email: student@demo.com     password: Student@123
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    // Always uses platform_db
    private final MongoTemplate platformMongoTemplate;

    // Tenant-aware — uses {tenantId}_db based on TenantContext
    private final MongoTemplate mongoTemplate;

    public DataInitializer(PasswordEncoder passwordEncoder,
                           @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate,
                           MongoTemplate mongoTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.platformMongoTemplate = platformMongoTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("[DataInitializer] Running startup data seeding...");
        seedSuperAdmin();
        seedTestSchool();
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

    /**
     * Seeds a complete test school with admin, teacher, student, and AI config.
     * Idempotent — skips if the "demo" school already exists.
     */
    private void seedTestSchool() {
        String tenantId = "demo";

        // Check if demo school already exists in platform_db
        boolean schoolExists = platformMongoTemplate.exists(
                Query.query(Criteria.where("tenantId").is(tenantId)), School.class);

        if (schoolExists) {
            log.info("[DataInitializer] Test school 'demo' already exists — skipping test data seed.");
            return;
        }

        log.info("[DataInitializer] Creating test school 'demo' with sample users...");

        // 1. Create School in platform_db
        School school = new School();
        school.setTenantId(tenantId);
        school.setName("Demo School");
        school.setAdminEmail("admin@demo.com");
        school.setPhone("9876543210");
        school.setCity("Mumbai");
        school.setState("Maharashtra");
        school.setBoard("CBSE");
        school.setPlan("free");
        school.setActive(true);
        school.setStudentCount(500);
        school.setCreatedAt(LocalDateTime.now());
        platformMongoTemplate.save(school);
        log.info("[DataInitializer] Created school: tenantId='{}', name='{}'", tenantId, school.getName());

        // 2. Switch to demo_db for tenant-specific data
        try {
            TenantContext.setTenant(tenantId);

            // 3. Create SCHOOL_ADMIN
            User admin = new User();
            admin.setEmail("admin@demo.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFullName("Demo School Admin");
            admin.setRole(UserRole.SCHOOL_ADMIN);
            admin.setTenantId(tenantId);
            admin.setActive(true);
            mongoTemplate.save(admin);
            log.info("[DataInitializer] Created SCHOOL_ADMIN: admin@demo.com");

            // 4. Create TEACHER
            User teacher = new User();
            teacher.setEmail("teacher@demo.com");
            teacher.setPassword(passwordEncoder.encode("Teacher@123"));
            teacher.setFullName("Mrs. Priya Sharma");
            teacher.setRole(UserRole.TEACHER);
            teacher.setTenantId(tenantId);
            teacher.setActive(true);
            mongoTemplate.save(teacher);
            log.info("[DataInitializer] Created TEACHER: teacher@demo.com (Mrs. Priya Sharma)");

            // 5. Create Student record
            Student student = new Student();
            student.setFullName("Rahul Kumar");
            student.setDateOfBirth(LocalDate.of(2012, 5, 15));
            student.setGender("Male");
            student.setClassForAdmission("Class 7 - A");
            student.setAcademicYear("2025-2026");
            student.setDateOfAdmission(LocalDate.of(2025, 4, 1));
            student.setAdmissionNumber("DEMO-001");
            student.setRollNumber("1");
            student.setStatus("ACTIVE");
            mongoTemplate.save(student);
            log.info("[DataInitializer] Created Student record: '{}' in '{}'", student.getFullName(), student.getClassForAdmission());

            // 6. Create STUDENT user (linked to Student record)
            User studentUser = new User();
            studentUser.setEmail("student@demo.com");
            studentUser.setPassword(passwordEncoder.encode("Student@123"));
            studentUser.setFullName("Rahul Kumar");
            studentUser.setRole(UserRole.STUDENT);
            studentUser.setTenantId(tenantId);
            studentUser.setLinkedEntityId(student.getId());
            studentUser.setActive(true);
            mongoTemplate.save(studentUser);
            log.info("[DataInitializer] Created STUDENT user: student@demo.com (linkedEntityId={})", student.getId());

            // 7. Create AI Config (enabled with Ollama for immediate testing)
            AiConfig aiConfig = new AiConfig();
            aiConfig.setTenantId(tenantId);
            aiConfig.setEnabled(true);
            aiConfig.setEnabledModes(List.of("TUTOR", "SOLVE", "PRACTICE"));
            aiConfig.setPrimaryProvider("OLLAMA");
            aiConfig.setFallbackProvider(null);
            aiConfig.setOllamaBaseUrl("http://localhost:11434");
            aiConfig.setOllamaModel("llama3");
            aiConfig.setDailyLimitPerStudent(50);
            aiConfig.setMaxConversationTurns(30);
            aiConfig.setUpdatedBy(admin.getId());
            aiConfig.setUpdatedAt(LocalDateTime.now());
            mongoTemplate.save(aiConfig);
            log.info("[DataInitializer] Created AI Config: enabled=true, provider=OLLAMA, model=llama3");

        } finally {
            TenantContext.clear();
        }

        // Log all credentials
        log.warn("=================================================================");
        log.warn("  TEST SCHOOL CREATED — For development/testing only!");
        log.warn("  ");
        log.warn("  School Code : demo");
        log.warn("  School Name : Demo School");
        log.warn("  ");
        log.warn("  SCHOOL ADMIN");
        log.warn("    Email   : admin@demo.com");
        log.warn("    Password: Admin@123");
        log.warn("  ");
        log.warn("  TEACHER");
        log.warn("    Email   : teacher@demo.com");
        log.warn("    Password: Teacher@123");
        log.warn("    Name    : Mrs. Priya Sharma");
        log.warn("  ");
        log.warn("  STUDENT");
        log.warn("    Email   : student@demo.com");
        log.warn("    Password: Student@123");
        log.warn("    Name    : Rahul Kumar");
        log.warn("    Class   : Class 7 - A");
        log.warn("  ");
        log.warn("  AI Helper : ENABLED (Ollama, all 3 modes)");
        log.warn("  ");
        log.warn("  HOW TO TEST:");
        log.warn("  1. Login as teacher@demo.com → Homework → Assign to 'Class 7 - A'");
        log.warn("  2. Login as student@demo.com → See homework → Tap 'Ask AI'");
        log.warn("  3. For AI to work: ollama pull llama3 && ollama serve");
        log.warn("=================================================================");
    }
}
