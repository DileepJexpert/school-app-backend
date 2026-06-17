package com.school.manage.controller;

import com.school.manage.model.SchoolWebsite;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class SchoolWebsiteController {

    private final MongoTemplate mongoTemplate;

    public SchoolWebsiteController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Public endpoint — fetches a school's website configuration by tenant ID.
     * No authentication required so any visitor can see the school's website.
     */
    @GetMapping("/public/website/{tenantId}")
    public ResponseEntity<SchoolWebsite> getWebsite(@PathVariable String tenantId) {
        String prev = TenantContext.getTenant();
        try {
            TenantContext.setTenant(tenantId);
            SchoolWebsite website = mongoTemplate.findOne(
                    Query.query(Criteria.where("tenantId").is(tenantId)),
                    SchoolWebsite.class);
            if (website == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(website);
        } finally {
            if (prev != null) TenantContext.setTenant(prev);
            else TenantContext.clear();
        }
    }

    /**
     * Admin endpoint — get the current school's website config.
     */
    @GetMapping("/api/website")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<SchoolWebsite> getOwnWebsite() {
        String tenantId = TenantContext.getTenant();
        SchoolWebsite website = mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(tenantId)),
                SchoolWebsite.class);
        if (website == null) {
            // Return an empty config seeded with tenantId so admin can fill it
            SchoolWebsite empty = new SchoolWebsite();
            empty.setTenantId(tenantId);
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(website);
    }

    /**
     * Admin endpoint — update the school's website configuration.
     */
    @PutMapping("/api/website")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    public ResponseEntity<SchoolWebsite> updateWebsite(@RequestBody SchoolWebsite website) {
        String tenantId = TenantContext.getTenant();
        website.setTenantId(tenantId);
        website.setUpdatedAt(LocalDateTime.now());

        // Upsert: find existing and preserve ID
        SchoolWebsite existing = mongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(tenantId)),
                SchoolWebsite.class);
        if (existing != null) {
            website.setId(existing.getId());
        }

        mongoTemplate.save(website);
        log.info("[SchoolWebsiteController] Updated website config for tenant '{}'", tenantId);
        return ResponseEntity.ok(website);
    }
}
