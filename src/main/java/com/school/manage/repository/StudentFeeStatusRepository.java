package com.school.manage.repository;


import com.school.manage.model.StudentFeeStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface StudentFeeStatusRepository extends MongoRepository<StudentFeeStatus, String> {
    Optional<StudentFeeStatus> findByStudentIdAndAcademicYear(String studentId, String academicYear);
}