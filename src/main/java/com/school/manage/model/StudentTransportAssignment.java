package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "student_transport_assignments")
public class StudentTransportAssignment {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String className;
    private String rollNumber;

    private String busId;
    private String routeId;

    /** The stop where this student boards the bus. */
    private String pickupStop;

    /** ACTIVE | INACTIVE */
    private String status = "ACTIVE";

    private LocalDate assignedDate;
    private LocalDateTime createdAt = LocalDateTime.now();
}
