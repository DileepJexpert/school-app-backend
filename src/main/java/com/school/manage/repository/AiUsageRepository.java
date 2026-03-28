package com.school.manage.repository;

import com.school.manage.model.AiUsageRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AiUsageRepository extends MongoRepository<AiUsageRecord, String> {

    long countByStudentIdAndRequestTimestampAfter(String studentId, LocalDateTime after);

    List<AiUsageRecord> findByStudentIdOrderByRequestTimestampDesc(String studentId);

    List<AiUsageRecord> findByRequestTimestampBetween(LocalDateTime from, LocalDateTime to);
}
