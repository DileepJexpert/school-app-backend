package com.school.manage.repository;


import com.school.manage.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {
    List<Student> findByFullNameContainingIgnoreCase(String name);

    /**
     * Finds all students admitted to a specific class in a specific academic year.
     * Used by FeeStructureService to retroactively create fee profiles when a
     * fee structure is set up after students have already been admitted.
     */
    List<Student> findByClassForAdmissionAndAcademicYear(String classForAdmission, String academicYear);
}