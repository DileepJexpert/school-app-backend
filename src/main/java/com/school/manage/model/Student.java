package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Document(collection = "students")
public class Student {

    @Id
    private String id;

    // --- Student Information ---
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String nationality;
    private String religion;
    private String motherTongue;
    private String aadharNumber;

    // --- Admission Details ---
    private String classForAdmission;
    private String academicYear;
    private LocalDate dateOfAdmission;
    private String admissionNumber;

    // --- Embedded Objects for nested details ---
    private ParentDetails parentDetails;
    private ContactDetails contactDetails;
    private PreviousSchoolDetails previousSchoolDetails;

    // --- Status Field for managing student lifecycle ---
    private String status = "ACTIVE"; // e.g., ACTIVE, WITHDRAWN, GRADUATED
}