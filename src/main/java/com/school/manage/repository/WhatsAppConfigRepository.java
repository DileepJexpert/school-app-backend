package com.school.manage.repository;

import com.school.manage.model.WhatsAppConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WhatsAppConfigRepository extends MongoRepository<WhatsAppConfig, String> {
    Optional<WhatsAppConfig> findByTenantId(String tenantId);
}
