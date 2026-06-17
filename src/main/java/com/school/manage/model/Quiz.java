package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "quizzes")
public class Quiz {
    @Id
    private String id;
    private String title;
    private String description;
    private String subject;
    private String className;
    private String chapter;
    private String academicYear;
    private int timeLimit;
    private String difficulty;
    private List<Question> questions;
    private boolean published = false;
    private boolean shuffleQuestions = true;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class Question {
        private String id;
        private String questionText;
        private List<String> options;
        private int correctOption;
        private String explanation;
        private int marks;
    }
}
