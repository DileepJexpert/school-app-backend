package com.school.manage.repository;

import com.school.manage.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    List<ChatMessage> findByRoomIdAndReadFalseAndSenderIdNot(String roomId, String userId);

    long countByRoomIdAndReadFalseAndSenderIdNot(String roomId, String userId);
}
