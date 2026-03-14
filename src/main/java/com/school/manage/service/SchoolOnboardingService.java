package com.school.manage.service;

import com.school.manage.model.School;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages school registration and the platform-level school registry.
 *
 * Uses platformMongoTemplate so all operations target platform_db
 * regardless of the current TenantContext.
 */
@Slf4j
@Service
public class SchoolOnboardingService {

    private final MongoTemplate platformMongoTemplate;

    public SchoolOnboardingService(@Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.platformMongoTemplate = platformMongoTemplate;
    }

    /**
     * Registers a new school on the platform.
     * Creates its entry in platform_db and MongoDB automatically
     * creates the tenant database on first write.
     */
    public School registerSchool(School school) {
        // Sanitize tenantId: lowercase, alphanumeric + underscore only
        String tenantId = school.getTenantId()
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_]", "_");
        school.setTenantId(tenantId);

        // Check uniqueness
        if (existsByTenantId(tenantId)) {
            throw new IllegalArgumentException("School with tenantId '" + tenantId + "' already exists.");
        }

        school.setCreatedAt(LocalDateTime.now());
        school.setActive(true);

        School saved = platformMongoTemplate.save(school);
        log.info("Registered new school: {} (tenantId={})", saved.getName(), saved.getTenantId());

        return saved;
    }

    /** Returns all registered schools */
    public List<School> getAllSchools() {
        return platformMongoTemplate.findAll(School.class);
    }

    /** Finds a school by its tenantId */
    public School getByTenantId(String tenantId) {
        Query query = new Query(Criteria.where("tenantId").is(tenantId));
        return platformMongoTemplate.findOne(query, School.class);
    }

    /** Activates or deactivates a school account */
    public School setActive(String tenantId, boolean active) {
        School school = getByTenantId(tenantId);
        if (school == null) {
            throw new IllegalArgumentException("School not found: " + tenantId);
        }
        school.setActive(active);
        return platformMongoTemplate.save(school);
    }

    /** Checks whether a tenant ID is already registered */
    public boolean existsByTenantId(String tenantId) {
        Query query = new Query(Criteria.where("tenantId").is(tenantId));
        return platformMongoTemplate.exists(query, School.class);
    }

    /** Validates that a tenant is registered and active (used by TenantInterceptor guard) */
    public boolean isActiveTenant(String tenantId) {
        Query query = new Query(
                Criteria.where("tenantId").is(tenantId).and("active").is(true)
        );
        return platformMongoTemplate.exists(query, School.class);
    }
}
