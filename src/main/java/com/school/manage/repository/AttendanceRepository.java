package com.school.manage.repository;

import com.school.manage.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    List<Attendance> findByStudentIdAndDateBetween(String studentId, LocalDate from, LocalDate to);

    List<Attendance> findByClassNameAndDate(String className, LocalDate date);

    List<Attendance> findByStudentIdAndAcademicYear(String studentId, String academicYear);

    Optional<Attendance> findByStudentIdAndDate(String studentId, LocalDate date);

    List<Attendance> findByClassNameAndAcademicYearAndDateBetween(
            String className, String academicYear, LocalDate from, LocalDate to);
}
