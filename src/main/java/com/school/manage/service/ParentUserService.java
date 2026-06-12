package com.school.manage.service;

import com.school.manage.enums.UserRole;
import com.school.manage.model.ParentDetails;
import com.school.manage.model.Student;
import com.school.manage.model.User;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class ParentUserService {

    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;

    public ParentUserService(MongoTemplate mongoTemplate, PasswordEncoder passwordEncoder) {
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Auto-creates or links parent user accounts based on the student's parentDetails.
     * Called after student admission or update.
     */
    public void ensureParentAccounts(Student student) {
        if (student == null || student.getId() == null) return;
        ParentDetails pd = student.getParentDetails();
        if (pd == null) return;

        String tenantId = TenantContext.getTenant();

        // Father account
        String fatherEmail = normalize(pd.getFatherEmail());
        String fatherPhone = normalize(pd.getFatherMobile());
        if (fatherEmail != null || fatherPhone != null) {
            createOrLink(fatherEmail, fatherPhone, pd.getFatherName(), student.getId(), tenantId);
        }

        // Mother account
        String motherEmail = normalize(pd.getMotherEmail());
        String motherPhone = normalize(pd.getMotherMobile());
        if (motherEmail != null || motherPhone != null) {
            // Avoid creating duplicate if mother and father share the same email/phone
            boolean sameAsFather = (fatherEmail != null && fatherEmail.equals(motherEmail))
                    || (fatherPhone != null && fatherPhone.equals(motherPhone));
            if (!sameAsFather) {
                createOrLink(motherEmail, motherPhone, pd.getMotherName(), student.getId(), tenantId);
            }
        }
    }

    private void createOrLink(String email, String phone, String name, String studentId, String tenantId) {
        // Try to find existing parent by email or phone
        User existing = findExistingParent(email, phone);

        if (existing != null) {
            // Link additional child
            String linked = existing.getLinkedEntityId();
            if (linked == null || linked.isBlank()) {
                existing.setLinkedEntityId(studentId);
            } else {
                // Check if already linked
                boolean alreadyLinked = Arrays.asList(linked.split(",")).contains(studentId);
                if (!alreadyLinked) {
                    existing.setLinkedEntityId(linked + "," + studentId);
                }
            }
            mongoTemplate.save(existing);
            log.info("[ParentUserService] Linked student '{}' to existing parent user '{}'",
                    studentId, existing.getEmail() != null ? existing.getEmail() : existing.getPhone());
            return;
        }

        // Create new parent user
        User parent = new User();
        parent.setEmail(email);
        parent.setPhone(phone);
        parent.setFullName(name != null ? name : "Parent");
        parent.setRole(UserRole.PARENT);
        parent.setTenantId(tenantId);
        parent.setLinkedEntityId(studentId);
        parent.setActive(true);

        // Default password: phone number if available, otherwise "Parent@123"
        String defaultPassword = phone != null ? phone : "Parent@123";
        parent.setPassword(passwordEncoder.encode(defaultPassword));

        mongoTemplate.save(parent);
        log.info("[ParentUserService] Created PARENT user: email='{}', phone='{}', name='{}', linkedStudent='{}'",
                email, phone, name, studentId);
    }

    private User findExistingParent(String email, String phone) {
        if (email != null) {
            User user = mongoTemplate.findOne(
                    Query.query(Criteria.where("email").is(email).and("role").is(UserRole.PARENT)),
                    User.class);
            if (user != null) return user;
        }
        if (phone != null) {
            User user = mongoTemplate.findOne(
                    Query.query(Criteria.where("phone").is(phone).and("role").is(UserRole.PARENT)),
                    User.class);
            if (user != null) return user;
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
