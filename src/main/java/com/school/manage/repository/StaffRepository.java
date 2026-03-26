package com.school.manage.repository;

import com.school.manage.model.Staff;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends MongoRepository<Staff, String> {

    List<Staff> findByDepartment(String department);

    List<Staff> findByStatus(String status);

    List<Staff> findByFullNameContainingIgnoreCase(String name);

    Optional<Staff> findByEmployeeId(String employeeId);

    Optional<Staff> findByEmail(String email);

    long countByStatus(String status);

    long countByDepartment(String department);
}
