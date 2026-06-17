package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.StudyMaterial;
import com.school.manage.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyMaterialService {

    private final StudyMaterialRepository studyMaterialRepository;

    public StudyMaterial createMaterial(StudyMaterial material) {
        material.setCreatedAt(LocalDateTime.now());
        log.info("[StudyMaterialService] Creating study material '{}' for class {}", material.getTitle(), material.getClassName());
        return studyMaterialRepository.save(material);
    }

    public StudyMaterial updateMaterial(String id, StudyMaterial material) {
        StudyMaterial existing = studyMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found: " + id));
        material.setId(id);
        material.setCreatedAt(existing.getCreatedAt());
        material.setUpdatedAt(LocalDateTime.now());
        log.info("[StudyMaterialService] Updating study material '{}'", id);
        return studyMaterialRepository.save(material);
    }

    public void deleteMaterial(String id) {
        if (!studyMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Study material not found: " + id);
        }
        studyMaterialRepository.deleteById(id);
        log.info("[StudyMaterialService] Deleted study material '{}'", id);
    }

    public StudyMaterial getMaterialById(String id) {
        return studyMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found: " + id));
    }

    public List<StudyMaterial> getMaterialsByClass(String className) {
        return studyMaterialRepository.findByClassNameAndPublishedTrueOrderBySortOrderAsc(className);
    }

    public List<StudyMaterial> getMaterialsByClassAndSubject(String className, String subject) {
        return studyMaterialRepository.findByClassNameAndSubjectAndPublishedTrueOrderBySortOrderAsc(className, subject);
    }

    public List<StudyMaterial> getMaterialsByTeacher(String teacherName) {
        return studyMaterialRepository.findByUploadedByOrderByCreatedAtDesc(teacherName);
    }

    public List<StudyMaterial> getAllMaterials() {
        return studyMaterialRepository.findAll();
    }
}
