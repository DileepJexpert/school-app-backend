package com.school.manage.repository;

import com.school.manage.model.StudyMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudyMaterialRepository extends MongoRepository<StudyMaterial, String> {
    List<StudyMaterial> findByClassNameAndPublishedTrueOrderBySortOrderAsc(String className);
    List<StudyMaterial> findByClassNameAndSubjectAndPublishedTrueOrderBySortOrderAsc(String className, String subject);
    List<StudyMaterial> findByClassNameAndAcademicYearAndPublishedTrueOrderBySortOrderAsc(String className, String year);
    List<StudyMaterial> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);
    long countByClassNameAndPublishedTrue(String className);
}
