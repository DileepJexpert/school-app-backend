package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "daily_diary")
public class DailyDiary {

    @Id
    private String id;

    private String className;       // "Class 5", "Class 10"
    private String section;         // "A", "B" (optional)
    private String subject;
    private LocalDate date;
    private String title;           // summary title
    private String content;         // detailed notes
    private String homework;        // any homework given
    private String teacherName;
    private String teacherId;       // user ID of teacher
    private String academicYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
