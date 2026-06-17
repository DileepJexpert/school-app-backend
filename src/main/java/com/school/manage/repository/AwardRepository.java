package com.school.manage.repository;

import com.school.manage.model.Award;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AwardRepository extends MongoRepository<Award, String> {

    List<Award> findByStudentId(String studentId);

    List<Award> findByClassName(String className);

    List<Award> findByCategory(String category);

    List<Award> findByLevel(String level);

    List<Award> findByAcademicYear(String academicYear);

    List<Award> findByStudentIdAndAcademicYear(String studentId, String academicYear);

    long countByStudentId(String studentId);

    List<Award> findAllByOrderByDateAwardedDesc();
}
