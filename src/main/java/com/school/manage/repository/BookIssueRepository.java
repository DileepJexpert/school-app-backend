package com.school.manage.repository;

import com.school.manage.model.BookIssue;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookIssueRepository extends MongoRepository<BookIssue, String> {
    List<BookIssue> findByStudentIdAndStatus(String studentId, String status);
    List<BookIssue> findByStudentId(String studentId);
    List<BookIssue> findByBookIdAndStatus(String bookId, String status);
    List<BookIssue> findByStatus(String status);
    List<BookIssue> findByDueDateBeforeAndStatus(LocalDate date, String status);
    long countByStudentIdAndStatus(String studentId, String status);
}
