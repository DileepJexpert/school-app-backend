package com.school.manage.repository;

import com.school.manage.model.LeaveRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends MongoRepository<LeaveRequest, String> {

    List<LeaveRequest> findByStaffId(String staffId);

    List<LeaveRequest> findByStatus(String status);

    List<LeaveRequest> findByStaffIdAndStatus(String staffId, String status);

    List<LeaveRequest> findByStaffIdAndFromDateBetween(String staffId, LocalDate from, LocalDate to);

    long countByStatus(String status);
}
