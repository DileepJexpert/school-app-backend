package com.school.manage.repository;

import com.school.manage.model.TutorialVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TutorialVideoRepository extends MongoRepository<TutorialVideo, String> {

    List<TutorialVideo> findByClassNameAndStatusOrderByCreatedAtDesc(String className, String status);

    List<TutorialVideo> findByClassNameAndSubjectAndStatusOrderByCreatedAtDesc(
            String className, String subject, String status);

    List<TutorialVideo> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<TutorialVideo> findAllByOrderByCreatedAtDesc();
}
