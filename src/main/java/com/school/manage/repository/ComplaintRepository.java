package com.school.manage.repository;

import com.school.manage.model.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComplaintRepository extends MongoRepository<Complaint, String> {

    List<Complaint> findByFiledBy(String userId);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByCategory(String category);

    List<Complaint> findByPriority(String priority);

    List<Complaint> findByStudentId(String studentId);

    List<Complaint> findByAssignedTo(String userId);

    long countByStatus(String status);

    List<Complaint> findAllByOrderByCreatedAtDesc();
}
