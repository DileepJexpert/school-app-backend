package com.school.manage.repository;

import com.school.manage.model.AiConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AiConfigRepository extends MongoRepository<AiConfig, String> {
    Optional<AiConfig> findByTenantId(String tenantId);
}
