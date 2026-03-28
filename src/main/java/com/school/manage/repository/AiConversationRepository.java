package com.school.manage.repository;

import com.school.manage.model.AiConversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends MongoRepository<AiConversation, String> {

    List<AiConversation> findByStudentIdOrderByLastMessageAtDesc(String studentId);

    Optional<AiConversation> findByStudentIdAndHomeworkIdAndMode(
            String studentId, String homeworkId, String mode);

    List<AiConversation> findByStudentIdAndHomeworkId(String studentId, String homeworkId);
}
