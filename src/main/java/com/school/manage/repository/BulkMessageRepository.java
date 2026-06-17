package com.school.manage.repository;

import com.school.manage.model.BulkMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BulkMessageRepository extends MongoRepository<BulkMessage, String> {
    List<BulkMessage> findAllByOrderByCreatedAtDesc();
    List<BulkMessage> findByStatus(String status);
}
