package com.school.manage.repository;

import com.school.manage.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByClassNameAndPublishedTrueOrderByCreatedAtDesc(String className);
    List<Quiz> findByClassNameAndSubjectAndPublishedTrueOrderByCreatedAtDesc(String className, String subject);
    List<Quiz> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    List<Quiz> findAllByOrderByCreatedAtDesc();
}
