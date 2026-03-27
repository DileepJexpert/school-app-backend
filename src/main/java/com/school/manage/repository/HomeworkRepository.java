package com.school.manage.repository;

import com.school.manage.model.Homework;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HomeworkRepository extends MongoRepository<Homework, String> {

    List<Homework> findByClassNameAndStatusOrderByDueDateDesc(String className, String status);

    List<Homework> findByClassNameAndAcademicYearAndStatusOrderByDueDateDesc(
            String className, String academicYear, String status);

    List<Homework> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<Homework> findByClassNameOrderByDueDateDesc(String className);

    List<Homework> findAllByOrderByCreatedAtDesc();
}
