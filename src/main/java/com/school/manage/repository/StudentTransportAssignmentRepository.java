package com.school.manage.repository;

import com.school.manage.model.StudentTransportAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentTransportAssignmentRepository
        extends MongoRepository<StudentTransportAssignment, String> {

    List<StudentTransportAssignment> findByBusIdAndStatus(String busId, String status);

    List<StudentTransportAssignment> findByRouteIdAndStatus(String routeId, String status);

    Optional<StudentTransportAssignment> findByStudentIdAndStatus(String studentId, String status);

    long countByBusIdAndStatus(String busId, String status);

    long countByRouteIdAndStatus(String routeId, String status);

    boolean existsByStudentIdAndStatus(String studentId, String status);
}
