package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "timetables")
public class Timetable {

    @Id
    private String id;

    private String className;
    private String academicYear;

    // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    private String dayOfWeek;

    private List<Period> periods;

    @Data
    public static class Period {
        private int periodNumber;
        private String subject;
        private String teacherName;
        // Format: "HH:mm" e.g. "09:00"
        private String startTime;
        private String endTime;
    }
}
