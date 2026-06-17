package com.school.manage.controller;

import com.school.manage.model.Event;
import com.school.manage.model.School;
import com.school.manage.service.EventService;
import com.school.manage.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Public (unauthenticated) endpoint for school website visitors to see upcoming events.
 * Security: /public/** is already permitted in SecurityConfig.
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class PublicEventController {

    private final EventService eventService;
    private final MongoTemplate platformMongoTemplate;

    public PublicEventController(EventService eventService,
                                 @Qualifier("platformMongoTemplate") MongoTemplate platformMongoTemplate) {
        this.eventService = eventService;
        this.platformMongoTemplate = platformMongoTemplate;
    }

    /**
     * Returns upcoming events for a school's public website.
     * No authentication required.
     *
     * @param tenantId the school's tenant identifier
     * @return list of upcoming events
     */
    @GetMapping("/public/events/{tenantId}")
    public ResponseEntity<List<Event>> getPublicEvents(@PathVariable String tenantId) {
        validateTenant(tenantId);
        String prev = TenantContext.getTenant();
        try {
            TenantContext.setTenant(tenantId);
            return ResponseEntity.ok(eventService.getUpcomingEvents());
        } finally {
            if (prev != null) {
                TenantContext.setTenant(prev);
            } else {
                TenantContext.clear();
            }
        }
    }

    /**
     * Returns all holidays for a school's public website.
     * No authentication required.
     *
     * @param tenantId the school's tenant identifier
     * @return list of holidays
     */
    @GetMapping("/public/events/{tenantId}/holidays")
    public ResponseEntity<List<Event>> getPublicHolidays(@PathVariable String tenantId) {
        validateTenant(tenantId);
        String prev = TenantContext.getTenant();
        try {
            TenantContext.setTenant(tenantId);
            return ResponseEntity.ok(eventService.getHolidays());
        } finally {
            if (prev != null) {
                TenantContext.setTenant(prev);
            } else {
                TenantContext.clear();
            }
        }
    }

    /**
     * Checks that the tenantId corresponds to an active school in platform_db.
     */
    private void validateTenant(String tenantId) {
        School school = platformMongoTemplate.findOne(
                Query.query(Criteria.where("tenantId").is(tenantId).and("active").is(true)),
                School.class);
        if (school == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "School not found or inactive for tenant: " + tenantId);
        }
    }
}
