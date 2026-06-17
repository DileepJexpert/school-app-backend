package com.school.manage.repository;

import com.school.manage.model.StaffAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface StaffAttendanceRepository extends MongoRepository<StaffAttendance, String> {

    List<StaffAttendance> findByDate(LocalDate date);

    List<StaffAttendance> findByUserId(String userId);

    List<StaffAttendance> findByUserIdAndDateBetween(String userId, LocalDate from, LocalDate to);

    List<StaffAttendance> findByDateAndStatus(LocalDate date, String status);

    List<StaffAttendance> findByDateBetween(LocalDate from, LocalDate to);

    long countByDateAndStatus(LocalDate date, String status);
}
