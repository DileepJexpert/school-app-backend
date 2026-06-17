package com.school.manage.repository;

import com.school.manage.model.Notice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoticeRepository extends MongoRepository<Notice, String> {

    List<Notice> findByPublishedTrueOrderByPinnedDescCreatedAtDesc();

    List<Notice> findByTargetAudience(String audience);

    List<Notice> findByCategory(String category);

    List<Notice> findByTargetClass(String className);

    List<Notice> findByPublishedByIdOrderByCreatedAtDesc(String userId);

    long countByPublishedTrue();
}
