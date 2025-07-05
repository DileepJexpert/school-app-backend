package com.school.manage.repository;

import com.school.manage.model.StudentResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentResultRepository extends MongoRepository<StudentResult, String> {
    List<StudentResult> findByClassNameAndYear(String className, int year);
    List<StudentResult> findByStudentId(String studentId);
}