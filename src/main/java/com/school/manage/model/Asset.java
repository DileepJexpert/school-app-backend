package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "assets")
public class Asset {
    @Id
    private String id;
    private String name;
    private String category;       // FURNITURE, ELECTRONICS, LAB_EQUIPMENT, SPORTS, VEHICLE, STATIONERY, OTHER
    private String assetCode;      // unique code like "AST-001"
    private String description;
    private String location;       // "Room 101", "Lab A", "Library"
    private int quantity;
    private double purchasePrice;
    private LocalDate purchaseDate;
    private String vendor;
    private String condition;      // NEW, GOOD, FAIR, POOR, DAMAGED, DISPOSED
    private LocalDate warrantyExpiry;
    private String assignedTo;     // department or person
    private String remarks;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
