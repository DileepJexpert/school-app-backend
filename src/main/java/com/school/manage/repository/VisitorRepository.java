package com.school.manage.repository;

import com.school.manage.model.Visitor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface VisitorRepository extends MongoRepository<Visitor, String> {

    List<Visitor> findByVisitDate(LocalDate date);

    List<Visitor> findByStatus(String status);

    List<Visitor> findByVisitDateAndStatus(LocalDate date, String status);

    List<Visitor> findByVisitorNameContainingIgnoreCase(String name);

    List<Visitor> findByPhoneContaining(String phone);

    long countByVisitDate(LocalDate date);

    List<Visitor> findByVisitDateBetween(LocalDate from, LocalDate to);
}
