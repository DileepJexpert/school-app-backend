package com.school.manage.dto;

import lombok.Data;
import java.util.List;

/**
 * Payload for bulk marks entry — submits marks for an entire class
 * for one exam type and one subject in a single API call.
 */
@Data
public class BulkResultRequest {

    private String className;
    private String examType;
    private String academicYear;
    private String subject;
    private double maxMarks;
    private String enteredBy;

    /** One entry per student in the class. */
    private List<StudentEntry> entries;

    @Data
    public static class StudentEntry {
        private String studentId;
        private String studentName;
        private String rollNumber;
        private double marksObtained;
        private String teacherRemarks;
    }
}
