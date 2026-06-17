package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Document(collection = "staff_attendance")
public class StaffAttendance {
    @Id
    private String id;
    private String userId;          // staff user ID
    private String staffName;
    private String department;      // Teaching, Admin, Support, etc.
    private String designation;
    private LocalDate date;
    private String status;          // PRESENT, ABSENT, HALF_DAY, ON_LEAVE, LATE
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String remarks;
    private String markedBy;        // admin who marked
    private String academicYear;
    private LocalDateTime createdAt;
}
