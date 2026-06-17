package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "quiz_attempts")
public class QuizAttempt {
    @Id
    private String id;
    private String quizId;
    private String quizTitle;
    private String studentId;
    private String studentName;
    private String className;
    private String subject;
    private List<Answer> answers;
    private int totalQuestions;
    private int correctAnswers;
    private int totalMarks;
    private int scoredMarks;
    private double percentage;
    private int timeTakenSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    public static class Answer {
        private String questionId;
        private int selectedOption;
        private boolean correct;
    }
}
