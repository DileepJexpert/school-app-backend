package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "homework")
public class Homework {

    @Id
    private String id;

    private String title;
    private String description;
    private String className;       // matches Student.classForAdmission
    private String subject;
    private String teacherName;
    private String teacherId;       // User ID of teacher
    private LocalDate dueDate;
    private LocalDate assignedDate;
    private String academicYear;
    private String status = "ACTIVE"; // ACTIVE, ARCHIVED
    private LocalDateTime createdAt;
}
