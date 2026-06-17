package com.school.manage.repository;

import com.school.manage.model.ExamSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ExamScheduleRepository extends MongoRepository<ExamSchedule, String> {
    List<ExamSchedule> findByAcademicYearOrderByCreatedAtDesc(String year);
    List<ExamSchedule> findByClassNameAndAcademicYear(String className, String year);
    List<ExamSchedule> findByExamTypeAndAcademicYear(String examType, String year);
    List<ExamSchedule> findByStatus(String status);
    List<ExamSchedule> findByClassNameAndAcademicYearAndStatus(String className, String year, String status);
}
