package com.school.manage.repository;

import com.school.manage.model.Payroll;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PayrollRepository extends MongoRepository<Payroll, String> {

    List<Payroll> findByMonthAndYear(String month, int year);

    List<Payroll> findByUserId(String userId);

    List<Payroll> findByUserIdAndYear(String userId, int year);

    List<Payroll> findByStatus(String status);

    List<Payroll> findByMonthAndYearAndStatus(String month, int year, String status);
}
