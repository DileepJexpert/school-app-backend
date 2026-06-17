package com.school.manage.repository;

import com.school.manage.model.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
    List<QuizAttempt> findByStudentIdOrderByCompletedAtDesc(String studentId);
    List<QuizAttempt> findByStudentIdAndSubject(String studentId, String subject);
    List<QuizAttempt> findByQuizId(String quizId);
    Optional<QuizAttempt> findByStudentIdAndQuizId(String studentId, String quizId);
    long countByStudentId(String studentId);
}
