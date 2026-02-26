package com.school.manage.repository;

import com.school.manage.model.Timetable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends MongoRepository<Timetable, String> {

    List<Timetable> findByClassNameAndAcademicYear(String className, String academicYear);

    Optional<Timetable> findByClassNameAndAcademicYearAndDayOfWeek(
            String className, String academicYear, String dayOfWeek);

    void deleteByClassNameAndAcademicYear(String className, String academicYear);
}
