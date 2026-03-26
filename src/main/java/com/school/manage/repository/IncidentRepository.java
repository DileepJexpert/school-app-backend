package com.school.manage.repository;

import com.school.manage.model.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncidentRepository extends MongoRepository<Incident, String> {

    List<Incident> findByStudentId(String studentId);

    List<Incident> findByClassName(String className);

    List<Incident> findBySeverity(String severity);

    List<Incident> findByClassNameAndAcademicYear(String className, String academicYear);

    List<Incident> findByIncidentDateBetween(LocalDate from, LocalDate to);

    long countBySeverity(String severity);

    long countByResolved(boolean resolved);
}
