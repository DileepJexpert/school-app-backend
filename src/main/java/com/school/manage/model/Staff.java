package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "staff")
public class Staff {

    @Id
    private String id;

    @Indexed(unique = true)
    private String employeeId;

    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;

    private String department;    // TEACHING, ADMINISTRATION, ACCOUNTS, TRANSPORT, SUPPORT
    private String designation;   // Principal, Vice Principal, Teacher, Clerk, Driver, etc.
    private String qualification;
    private String specialization;

    private LocalDate dateOfJoining;
    private LocalDate dateOfLeaving;

    private double basicSalary;
    private String bankAccountNumber;
    private String bankName;
    private String panNumber;

    private String address;
    private String emergencyContact;
    private String emergencyContactName;

    private String bloodGroup;
    private String aadharNumber;

    private String profilePhotoUrl;

    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, RESIGNED, TERMINATED

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
