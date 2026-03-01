package com.school.manage.repository;

import com.school.manage.model.CoscholasticAssessment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CoscholasticRepository extends MongoRepository<CoscholasticAssessment, String> {

    List<CoscholasticAssessment> findByStudentIdAndAcademicYear(String studentId, String academicYear);

    Optional<CoscholasticAssessment> findByStudentIdAndAcademicYearAndTerm(
            String studentId, String academicYear, String term);

    List<CoscholasticAssessment> findByClassNameAndAcademicYear(String className, String academicYear);
}
