package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

/**
 * Represents the primary document for a student in the 'students' collection.
 * This class holds all the core information about a student, captured during admission.
 */
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

    // --- Admission & Academic Details ---
    private String classForAdmission;
    private String academicYear;
    private LocalDate dateOfAdmission;
    private String admissionNumber;

    /**
     * The student's roll number in their assigned class.
     * This is a new field added for easy identification and for denormalization
     * into the StudentFeeProfile.
     */
    private String rollNumber;


    // --- Embedded Objects for nested details ---
    private ParentDetails parentDetails;
    private ContactDetails contactDetails;
    private PreviousSchoolDetails previousSchoolDetails;

    // --- Status Field for managing student lifecycle ---
    private String status = "ACTIVE"; // e.g., ACTIVE, WITHDRAWN, GRADUATED
}
