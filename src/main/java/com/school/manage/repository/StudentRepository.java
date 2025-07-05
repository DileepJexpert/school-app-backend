package com.school.manage.repository;


import com.school.manage.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {
    List<Student> findByFullNameContainingIgnoreCase(String name);
}