package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "maintenance_records")
public class MaintenanceRecord {
    @Id
    private String id;
    private String assetId;
    private String assetName;      // denormalized
    private String type;           // REPAIR, SERVICING, REPLACEMENT, INSPECTION
    private String description;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private double cost;
    private String performedBy;    // vendor or staff
    private String status;         // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    private String remarks;
    private LocalDateTime createdAt;
}
