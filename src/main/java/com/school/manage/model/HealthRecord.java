package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "health_records")
public class HealthRecord {

    @Id
    private String id;
    private String studentId;
    private String studentName;
    private String className;

    // Basic health info
    private String bloodGroup;         // A+, A-, B+, B-, O+, O-, AB+, AB-
    private double height;             // in cm
    private double weight;             // in kg
    private String vision;             // e.g. "6/6", "6/9"

    // Medical
    private List<String> allergies;           // food, drug, other allergies
    private List<String> chronicConditions;   // asthma, diabetes, etc.
    private String currentMedications;
    private List<String> vaccinationRecords;  // list of vaccines taken

    // Emergency
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String familyDoctor;
    private String familyDoctorPhone;

    // Visit log
    private List<MedicalVisit> medicalVisits;  // nested

    private String academicYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class MedicalVisit {
        private String id;      // UUID
        private LocalDate date;
        private String reason;
        private String symptoms;
        private String treatment;
        private String attendedBy;   // school nurse / doctor name
        private boolean sentHome;
        private String remarks;
    }
}
