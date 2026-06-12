package com.school.manage.repository;

import com.school.manage.model.WhatsAppConversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WhatsAppConversationRepository extends MongoRepository<WhatsAppConversation, String> {
    Optional<WhatsAppConversation> findByPhoneNumber(String phoneNumber);
    List<WhatsAppConversation> findByTenantIdOrderByLastMessageAtDesc(String tenantId);
}
