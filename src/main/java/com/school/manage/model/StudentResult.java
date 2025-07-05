package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Document(collection = "student_results")
public class StudentResult {
    @Id
    private String id;
    private String studentId;
    private String name;
    private String className;
    private int year;
    private Map<String, Integer> marks;
    private int total;
    private double percentage;
    private String grade;
}