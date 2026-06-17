package com.school.manage.repository;

import com.school.manage.model.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PromotionRepository extends MongoRepository<Promotion, String> {

    List<Promotion> findByFromAcademicYear(String year);

    List<Promotion> findByStudentId(String studentId);

    List<Promotion> findByFromClass(String className);

    List<Promotion> findByStatus(String status);

    List<Promotion> findByFromClassAndFromAcademicYear(String className, String year);

    long countByFromAcademicYearAndStatus(String year, String status);
}
