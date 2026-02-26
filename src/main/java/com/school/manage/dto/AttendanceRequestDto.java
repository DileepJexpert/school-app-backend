package com.school.manage.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceRequestDto {

    // Used for marking attendance for multiple students in one request
    private List<StudentAttendanceEntry> entries;
    private LocalDate date;
    private String className;
    private String academicYear;
    private String markedBy;

    @Data
    public static class StudentAttendanceEntry {
        private String studentId;
        private String studentName;
        // PRESENT, ABSENT, LATE, HALF_DAY
        private String status;
        private String remarks;
    }
}
