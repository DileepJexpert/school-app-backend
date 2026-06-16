package com.school.manage.dto;

import lombok.Data;

/**
 * DTO for the public online admission form.
 * Captures all information a parent would fill in when submitting
 * an admission enquiry for their child.
 */
@Data
public class PublicAdmissionRequest {

    // --- Student info (required: fullName, classForAdmission) ---
    private String fullName;
    private String dateOfBirth; // ISO string "YYYY-MM-DD", parsed to LocalDate
    private String gender;
    private String bloodGroup;
    private String nationality;
    private String religion;
    private String motherTongue;
    private String aadharNumber;
    private String classForAdmission; // e.g. "Class 5", "Nursery"
    private String academicYear;

    // --- Parent info (at least fatherMobile or motherMobile required) ---
    private String fatherName;
    private String fatherOccupation;
    private String fatherMobile;
    private String fatherEmail;
    private String motherName;
    private String motherOccupation;
    private String motherMobile;
    private String motherEmail;

    // --- Contact info ---
    private String address;
    private String primaryContactNumber;

    // --- Previous school (optional) ---
    private String previousSchoolName;
    private String previousClass;
    private String previousBoard;
}
