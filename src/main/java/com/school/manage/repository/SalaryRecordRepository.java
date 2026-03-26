package com.school.manage.repository;

import com.school.manage.model.SalaryRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends MongoRepository<SalaryRecord, String> {

    List<SalaryRecord> findByMonthAndYear(int month, int year);

    List<SalaryRecord> findByStaffId(String staffId);

    Optional<SalaryRecord> findByStaffIdAndMonthAndYear(String staffId, int month, int year);

    List<SalaryRecord> findByStatus(String status);
}
