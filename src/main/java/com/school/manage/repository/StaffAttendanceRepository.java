package com.school.manage.repository;

import com.school.manage.model.StaffAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StaffAttendanceRepository extends MongoRepository<StaffAttendance, String> {

    List<StaffAttendance> findByStaffIdAndDateBetween(String staffId, LocalDate from, LocalDate to);

    List<StaffAttendance> findByDate(LocalDate date);

    Optional<StaffAttendance> findByStaffIdAndDate(String staffId, LocalDate date);

    List<StaffAttendance> findByDepartmentAndDate(String department, LocalDate date);
}
