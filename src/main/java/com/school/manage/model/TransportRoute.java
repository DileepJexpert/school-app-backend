package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "transport_routes")
public class TransportRoute {

    @Id
    private String id;

    /** Short zone label, e.g. "Zone A" */
    private String zoneName;

    /** Full display name, e.g. "Zone A – North" */
    private String displayName;

    /** Comma-separated areas, e.g. "Rajpur, Saket, Vasant Kunj" */
    private String areasCovered;

    /** Ordered list of stop names. */
    private List<String> stops;

    /** First pickup time, e.g. "7:10 AM" */
    private String firstPickupTime;

    /** Monthly transport fee in INR. */
    private double monthlyFee;

    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Not persisted — computed by TransportService from active assignments
     * and injected before returning to client.
     */
    @Transient
    private int assignedCount;
}
