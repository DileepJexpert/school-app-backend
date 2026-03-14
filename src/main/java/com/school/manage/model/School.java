package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a registered school on the platform.
 * Stored in platform_db.schools — NOT in any tenant database.
 *
 * Each school gets its own isolated MongoDB database: {tenantId}_db
 */
@Data
@Document(collection = "schools")
public class School {

    @Id
    private String id;

    /** Unique short identifier used as DB name prefix.  e.g., "springfield", "dps_rohini" */
    @Indexed(unique = true)
    private String tenantId;

    /** Full school name */
    private String name;

    /** Primary contact email (admin) */
    private String adminEmail;

    /** Contact phone number */
    private String phone;

    /** City / district */
    private String city;

    /** State */
    private String state;

    /** Board: CBSE, ICSE, State Board, etc. */
    private String board;

    /** Subscription plan: free | basic | pro | enterprise */
    private String plan = "free";

    /** Whether the school account is active */
    private boolean active = true;

    /** Subscription expiry date (null = lifetime / trial) */
    private LocalDate subscriptionExpiry;

    /** Timestamp of when this school was onboarded */
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Optional: logo URL */
    private String logoUrl;

    /** Approximate student count (used for billing tier) */
    private int studentCount;
}
