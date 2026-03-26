package com.school.manage.repository;

import com.school.manage.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    List<ChatRoom> findByParticipantsContaining(String userId);

    Optional<ChatRoom> findByParticipantsContainingAndStudentId(List<String> participants, String studentId);
}
