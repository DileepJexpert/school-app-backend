package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "buses")
public class Bus {

    @Id
    private String id;

    private String busNumber;
    private String driverName;
    private String driverMobile;

    /** References TransportRoute.id — nullable (bus may not have a route yet). */
    private String routeId;

    private int capacity;

    /** ACTIVE | MAINTENANCE | RETIRED */
    private String status = "ACTIVE";

    private String insuranceExpiry;
    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Not persisted to MongoDB — computed by TransportService from the
     * student_transport_assignments collection and injected before returning.
     */
    @Transient
    private int assignedCount;
}
