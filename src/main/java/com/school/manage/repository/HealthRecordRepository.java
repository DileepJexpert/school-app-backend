package com.school.manage.repository;

import com.school.manage.model.HealthRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface HealthRecordRepository extends MongoRepository<HealthRecord, String> {

    Optional<HealthRecord> findByStudentId(String studentId);

    List<HealthRecord> findByClassName(String className);

    List<HealthRecord> findByBloodGroup(String bloodGroup);

    List<HealthRecord> findByAllergiesContaining(String allergy);
}
