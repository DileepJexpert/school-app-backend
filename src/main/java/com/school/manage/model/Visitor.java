package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "visitors")
public class Visitor {

    @Id
    private String id;

    private String visitorName;
    private String phone;
    private String email;           // optional
    private String purpose;         // PARENT_MEETING, VENDOR, OFFICIAL, OTHER
    private String purposeDetails;  // free text
    private String personToMeet;    // who they're visiting
    private String department;      // optional
    private int numberOfVisitors = 1;
    private String idProofType;     // AADHAAR, PAN, DRIVING_LICENSE, PASSPORT, OTHER
    private String idProofNumber;
    private String vehicleNumber;   // optional
    private String gatePassNumber;  // auto-generated: GP-001, GP-002
    private String status;          // CHECKED_IN, CHECKED_OUT, EXPECTED
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String checkedInBy;     // admin/guard who checked in
    private String remarks;         // optional
    private LocalDate visitDate;
    private LocalDateTime createdAt;
}
