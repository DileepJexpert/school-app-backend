package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "leave_requests")
public class LeaveRequest {

    @Id
    private String id;

    private String staffId;
    private String staffName;
    private String department;

    // CASUAL, SICK, EARNED, MATERNITY, PATERNITY, UNPAID, OTHER
    private String leaveType;

    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalDays;

    private String reason;

    // PENDING, APPROVED, REJECTED, CANCELLED
    private String status = "PENDING";

    private String approvedBy;
    private String approverRemarks;
    private LocalDateTime approvedAt;

    private LocalDateTime appliedAt = LocalDateTime.now();
}
