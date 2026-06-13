package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.model.AiConfig;
import com.school.manage.model.ParentDetails;
import com.school.manage.model.School;
import com.school.manage.model.SchoolWebsite;
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
import java.util.Map;

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

        boolean exists = platformMongoTemplate.exists(
                Query.query(Criteria.where("email").is(email)), User.class);

        if (exists) {
            log.info("[DataInitializer] SUPER_ADMIN already exists — skipping.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setEmail(email);
        superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
        superAdmin.setFullName("Platform Super Admin");
        superAdmin.setRole(UserRole.SUPER_ADMIN);
        superAdmin.setTenantId(null);
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
     * Each entity is checked independently — safe to run multiple times.
     */
    private void seedTestSchool() {
        String tenantId = "demo";
        boolean createdAnything = false;

        log.info("[DataInitializer] Checking test school 'demo'...");

        // 1. Create School in platform_db (if missing)
        boolean schoolExists = platformMongoTemplate.exists(
                Query.query(Criteria.where("tenantId").is(tenantId)), School.class);

        if (!schoolExists) {
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
            log.info("[DataInitializer] Created school: tenantId='demo', name='Demo School'");
            createdAnything = true;
        } else {
            log.info("[DataInitializer] School 'demo' already exists in platform_db.");
        }

        // 2. Switch to demo_db for tenant-specific data
        try {
            TenantContext.setTenant(tenantId);

            // 3. Create SCHOOL_ADMIN (if missing)
            if (!userExists("admin@demo.com")) {
                User admin = new User();
                admin.setEmail("admin@demo.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setFullName("Demo School Admin");
                admin.setRole(UserRole.SCHOOL_ADMIN);
                admin.setTenantId(tenantId);
                admin.setActive(true);
                mongoTemplate.save(admin);
                log.info("[DataInitializer] Created SCHOOL_ADMIN: admin@demo.com");
                createdAnything = true;
            }

            // 4. Create TEACHER (if missing)
            if (!userExists("teacher@demo.com")) {
                User teacher = new User();
                teacher.setEmail("teacher@demo.com");
                teacher.setPassword(passwordEncoder.encode("Teacher@123"));
                teacher.setFullName("Mrs. Priya Sharma");
                teacher.setRole(UserRole.TEACHER);
                teacher.setTenantId(tenantId);
                teacher.setActive(true);
                mongoTemplate.save(teacher);
                log.info("[DataInitializer] Created TEACHER: teacher@demo.com");
                createdAnything = true;
            }

            // 5. Create Student record + Student user (if missing)
            if (!userExists("student@demo.com")) {
                // Find or create Student record
                Student student = mongoTemplate.findOne(
                        Query.query(Criteria.where("admissionNumber").is("DEMO-001")),
                        Student.class);

                if (student == null) {
                    student = new Student();
                    student.setFullName("Rahul Kumar");
                    student.setDateOfBirth(LocalDate.of(2012, 5, 15));
                    student.setGender("Male");
                    student.setClassForAdmission("Class 7 - A");
                    student.setAcademicYear("2025-2026");
                    student.setDateOfAdmission(LocalDate.of(2025, 4, 1));
                    student.setAdmissionNumber("DEMO-001");
                    student.setRollNumber("1");
                    student.setStatus("ACTIVE");

                    ParentDetails parentDetails = new ParentDetails();
                    parentDetails.setFatherName("Mr. Rajesh Kumar");
                    parentDetails.setFatherMobile("9876543210");
                    parentDetails.setFatherEmail("parent@demo.com");
                    parentDetails.setFatherOccupation("Engineer");
                    parentDetails.setMotherName("Mrs. Sunita Kumar");
                    parentDetails.setMotherMobile("9876543211");
                    parentDetails.setMotherEmail("mother@demo.com");
                    parentDetails.setMotherOccupation("Teacher");
                    student.setParentDetails(parentDetails);

                    mongoTemplate.save(student);
                    log.info("[DataInitializer] Created Student: Rahul Kumar (Class 7 - A)");
                }

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
                createdAnything = true;
            }

            // 6. Create PARENT user (if missing)
            if (!userExists("parent@demo.com")) {
                // Find student to link
                Student demoStudent = mongoTemplate.findOne(
                        Query.query(Criteria.where("admissionNumber").is("DEMO-001")),
                        Student.class);

                User parentUser = new User();
                parentUser.setEmail("parent@demo.com");
                parentUser.setPhone("9876543210");
                parentUser.setPassword(passwordEncoder.encode("Parent@123"));
                parentUser.setFullName("Mr. Rajesh Kumar");
                parentUser.setRole(UserRole.PARENT);
                parentUser.setTenantId(tenantId);
                parentUser.setLinkedEntityId(demoStudent != null ? demoStudent.getId() : null);
                parentUser.setActive(true);
                mongoTemplate.save(parentUser);
                log.info("[DataInitializer] Created PARENT user: parent@demo.com (linkedEntityId={})",
                        parentUser.getLinkedEntityId());
                createdAnything = true;
            }

            // 7. Create AI Config (if missing)
            boolean aiConfigExists = mongoTemplate.exists(
                    Query.query(Criteria.where("tenantId").is(tenantId)), AiConfig.class);

            if (!aiConfigExists) {
                AiConfig aiConfig = new AiConfig();
                aiConfig.setTenantId(tenantId);
                aiConfig.setEnabled(true);
                aiConfig.setEnabledModes(List.of("TUTOR", "SOLVE", "PRACTICE"));
                aiConfig.setPrimaryProvider("OLLAMA");
                aiConfig.setFallbackProvider(null);
                aiConfig.setOllamaBaseUrl("http://localhost:11434");
                aiConfig.setOllamaModel("llama3.2:3b-instruct-q4_0");
                aiConfig.setDailyLimitPerStudent(50);
                aiConfig.setMaxConversationTurns(30);
                aiConfig.setUpdatedAt(LocalDateTime.now());
                mongoTemplate.save(aiConfig);
                log.info("[DataInitializer] Created AI Config: enabled=true, provider=OLLAMA");
                createdAnything = true;
            }

            // 8. Create School Website config (if missing)
            boolean websiteExists = mongoTemplate.exists(
                    Query.query(Criteria.where("tenantId").is(tenantId)), SchoolWebsite.class);

            if (!websiteExists) {
                SchoolWebsite website = new SchoolWebsite();
                website.setTenantId(tenantId);
                website.setSchoolName("Demo School");
                website.setShortName("DS");
                website.setTagline("Nurturing Minds, Building Futures");
                website.setAccreditation("CBSE Affiliated");
                website.setPhone("9876543210");
                website.setEmail("admin@demo.com");
                website.setAddress("Mumbai, Maharashtra, India");
                website.setOfficeHours("Mon – Fri: 8:00 AM – 4:00 PM\nSat: 9:00 AM – 1:00 PM");
                website.setPrimaryColor("#1B3A5C");
                website.setSecondaryColor("#C8922A");
                website.setMarqueeText("Welcome to Demo School — Admissions Open for 2026-27!");
                website.setPrincipalName("Dr. Amit Verma");
                website.setPrincipalTitle("Principal & Director");
                website.setPrincipalMessage("At Demo School, we believe every child carries within them "
                        + "the potential to change the world. Our approach combines time-honored educational "
                        + "values with modern pedagogical methods, ensuring our students are prepared not just "
                        + "for examinations, but for life itself.");
                website.setMission("To provide a holistic education that cultivates intellectual curiosity, "
                        + "ethical character, and global citizenship in every student.");
                website.setVision("To be a beacon of academic excellence and character formation, shaping "
                        + "leaders who contribute meaningfully to society.");

                website.setStats(List.of(
                        Map.of("value", "Since 2020", "label", "Established"),
                        Map.of("value", "500+", "label", "Students"),
                        Map.of("value", "30+", "label", "Faculty"),
                        Map.of("value", "2 Acre", "label", "Campus")
                ));

                website.setCoreValues(List.of(
                        Map.of("icon", "book", "title", "Academic Excellence", "description", "Rigorous curriculum designed to challenge and inspire."),
                        Map.of("icon", "shield", "title", "Integrity", "description", "Building character rooted in honesty and responsibility."),
                        Map.of("icon", "globe", "title", "Global Perspective", "description", "Preparing students for an interconnected world."),
                        Map.of("icon", "heart", "title", "Compassion", "description", "Fostering empathy and service to community.")
                ));

                website.setAchievements(List.of(
                        Map.of("year", "2025", "title", "National Science Olympiad — Gold Medal"),
                        Map.of("year", "2024", "title", "100% Pass Rate — Board Examinations"),
                        Map.of("year", "2024", "title", "Best School Award — District Level")
                ));

                website.setTestimonials(List.of(
                        Map.of("name", "Rajesh Kumar", "relation", "Parent of Rahul, Class 7", "text", "Demo School has been instrumental in shaping our son's confidence and academic abilities.")
                ));

                website.setEvents(List.of(
                        Map.of("date", "Mar 15, 2026", "title", "Annual Science Exhibition", "description", "Students showcase innovative science projects.", "category", "Academic"),
                        Map.of("date", "Apr 5, 2026", "title", "Sports Day", "description", "Annual athletics competition.", "category", "Sports"),
                        Map.of("date", "May 1, 2026", "title", "Admissions Open Day", "description", "Campus tour and admissions guidance.", "category", "Admissions")
                ));

                website.setNotices(List.of(
                        Map.of("date", "Feb 18, 2026", "title", "Admissions 2026-27 — Applications Now Open", "isHighPriority", "true")
                ));

                website.setAcademicLevels(List.of(
                        Map.of("id", "primary", "title", "Primary School", "grades", "Kindergarten – Grade 5", "focus", "Building strong foundations in literacy, numeracy, and social skills.", "highlights", "Phonics-based English|Hands-on Math|Environmental Studies|Art, Music & PE|Library & Computer Lab"),
                        Map.of("id", "middle", "title", "Middle School", "grades", "Grade 6 – Grade 8", "focus", "Expanding horizons with structured academics and critical thinking.", "highlights", "Advanced Science with labs|Foreign Languages|Robotics & Coding|Inter-house competitions|Career awareness"),
                        Map.of("id", "senior", "title", "Senior School", "grades", "Grade 9 – Grade 12", "focus", "Preparing for board examinations and higher education.", "highlights", "Science, Commerce & Humanities|Board exam preparation|College counseling|Research projects|Leadership programs")
                ));

                website.setCoCurriculars(List.of(
                        Map.of("icon", "science", "name", "Science Club"),
                        Map.of("icon", "music", "name", "Music & Band"),
                        Map.of("icon", "sports", "name", "Sports Academy"),
                        Map.of("icon", "art", "name", "Visual Arts")
                ));

                website.setFeeStructure(List.of(
                        Map.of("grade", "Primary (Grade 1–5)", "admission", "15,000", "tuition", "8,500 / month", "annual", "1,02,000"),
                        Map.of("grade", "Middle (Grade 6–8)", "admission", "18,000", "tuition", "10,500 / month", "annual", "1,26,000"),
                        Map.of("grade", "Senior (Grade 9–12)", "admission", "20,000", "tuition", "12,000 / month", "annual", "1,44,000")
                ));

                website.setAdmissionSteps(List.of(
                        Map.of("step", "1", "title", "Submit Application", "description", "Complete the online application form."),
                        Map.of("step", "2", "title", "Entrance Assessment", "description", "Age-appropriate assessment for academic readiness."),
                        Map.of("step", "3", "title", "Parent Interaction", "description", "Meeting with admissions committee."),
                        Map.of("step", "4", "title", "Offer & Enrollment", "description", "Complete fee payment to confirm enrollment.")
                ));

                website.setImportantDates(List.of(
                        Map.of("event", "Applications Open", "date", "February 1, 2026"),
                        Map.of("event", "Entrance Assessments", "date", "March 15–20, 2026"),
                        Map.of("event", "Session Begins", "date", "June 1, 2026")
                ));

                website.setTransportZones(List.of(
                        Map.of("zone", "Zone A", "area", "Nearby areas", "distance", "0–5 km", "fee", "2,500 / month"),
                        Map.of("zone", "Zone B", "area", "Extended areas", "distance", "5–10 km", "fee", "3,500 / month")
                ));

                website.setTransportFeatures(List.of(
                        "GPS-tracked buses with real-time parent app",
                        "Trained drivers with experience",
                        "Female attendant on every bus",
                        "First-aid kit and fire extinguisher equipped",
                        "CCTV surveillance on all vehicles"
                ));

                website.setTimeline(List.of(
                        Map.of("year", "2020", "text", "School founded with a vision for excellence")
                ));

                website.setGalleryImages(List.of(
                        Map.of("category", "Campus", "label", "Main Building", "color", "0xFF2C5F8A"),
                        Map.of("category", "Classroom", "label", "Smart Classroom", "color", "0xFF8A2C5F")
                ));

                website.setUpdatedAt(LocalDateTime.now());
                mongoTemplate.save(website);
                log.info("[DataInitializer] Created School Website config for 'demo'");
                createdAnything = true;
            }

        } finally {
            TenantContext.clear();
        }

        if (!createdAnything) {
            log.info("[DataInitializer] All test data for 'demo' already exists — nothing to create.");
            return;
        }

        // Log all credentials
        log.warn("=================================================================");
        log.warn("  TEST SCHOOL 'demo' — CREDENTIALS FOR TESTING");
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
        log.warn("  PARENT");
        log.warn("    Email   : parent@demo.com");
        log.warn("    Phone   : 9876543210");
        log.warn("    Password: Parent@123");
        log.warn("    Name    : Mr. Rajesh Kumar");
        log.warn("    Login   : Email or phone number");
        log.warn("  ");
        log.warn("  AI Helper : ENABLED (Ollama, all 3 modes)");
        log.warn("  ");
        log.warn("  HOW TO TEST:");
        log.warn("  1. Login as teacher@demo.com → Homework → Assign to 'Class 7 - A'");
        log.warn("  2. Login as student@demo.com → See homework → Tap 'Ask AI'");
        log.warn("  3. For AI to work: ollama pull llama3.2:3b-instruct-q4_0 && ollama serve");
        log.warn("  4. Login as parent@demo.com (or phone 9876543210) → Parent Portal");
        log.warn("=================================================================");
    }

    /** Check if a user with this email exists in the current tenant DB */
    private boolean userExists(String email) {
        return mongoTemplate.exists(
                Query.query(Criteria.where("email").is(email)), User.class);
    }
}
