package com.school.manage.repository;

import com.school.manage.model.StudentResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentResultRepository extends MongoRepository<StudentResult, String> {

    // ── Core queries ──────────────────────────────────────────────────────
    List<StudentResult> findByStudentId(String studentId);
    List<StudentResult> findByClassNameAndAcademicYear(String className, String academicYear);

    List<StudentResult> findByClassNameAndAcademicYearAndExamType(
            String className, String academicYear, String examType);

    List<StudentResult> findByClassNameAndAcademicYearAndExamTypeAndSubject(
            String className, String academicYear, String examType, String subject);

    List<StudentResult> findByStudentIdAndAcademicYear(String studentId, String academicYear);

    // ── Publish workflow ──────────────────────────────────────────────────
    List<StudentResult> findByClassNameAndAcademicYearAndExamTypeAndIsPublished(
            String className, String academicYear, String examType, boolean isPublished);

    // ── Bulk replace support ──────────────────────────────────────────────
    void deleteByClassNameAndAcademicYearAndExamTypeAndSubject(
            String className, String academicYear, String examType, String subject);
}
