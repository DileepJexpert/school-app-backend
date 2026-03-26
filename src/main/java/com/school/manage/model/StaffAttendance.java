package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "staff_attendance")
public class StaffAttendance {

    @Id
    private String id;

    private String staffId;
    private String staffName;
    private String department;
    private LocalDate date;

    // PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE
    private String status;

    private String checkInTime;  // HH:mm
    private String checkOutTime; // HH:mm

    private String remarks;
    private String markedBy;
    private LocalDateTime markedAt = LocalDateTime.now();
}
