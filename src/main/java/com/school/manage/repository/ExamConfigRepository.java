package com.school.manage.repository;

import com.school.manage.model.ExamConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ExamConfigRepository extends MongoRepository<ExamConfig, String> {

    List<ExamConfig> findByAcademicYear(String academicYear);

    List<ExamConfig> findByAcademicYearAndIsActive(String academicYear, boolean isActive);

    Optional<ExamConfig> findByAcademicYearAndExamType(String academicYear, String examType);
}
