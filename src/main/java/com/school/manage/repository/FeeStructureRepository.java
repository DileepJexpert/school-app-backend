package com.school.manage.repository;


import com.school.manage.model.FeeStructure;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FeeStructureRepository extends MongoRepository<FeeStructure, String> {
    List<FeeStructure> findByAcademicYear(String academicYear);
    FeeStructure findByAcademicYearAndClassName(String academicYear, String className);
}