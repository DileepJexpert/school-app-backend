package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String className;
    private String academicYear;
    private LocalDate date;

    // PRESENT, ABSENT, LATE, HALF_DAY
    private String status;

    private String markedBy;
    private String remarks;
    private LocalDateTime markedAt = LocalDateTime.now();
}
