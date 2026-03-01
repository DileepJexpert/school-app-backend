package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Co-scholastic assessment — inspired by CBSE's holistic evaluation model.
 * Captures non-academic dimensions: Art, Sports, Life Skills, Values, Health.
 * Grade scale: A (Outstanding) → B (Good) → C (Satisfactory) → D (Needs Improvement)
 */
@Data
@Document(collection = "coscholastic_assessments")
public class CoscholasticAssessment {

    @Id
    private String id;

    private String studentId;
    private String studentName;
    private String className;
    private String academicYear;
    private String term;            // TERM_1 | TERM_2

    private List<CoscholasticArea> areas;

    private String enteredBy;
    private LocalDateTime createdAt;

    @Data
    public static class CoscholasticArea {
        private String name;    // Art Education | Sports & Games | Life Skills | Values | Health
        private String grade;   // A | B | C | D
        private String remarks;
    }
}
