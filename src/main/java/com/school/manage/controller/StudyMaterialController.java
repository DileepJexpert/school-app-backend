package com.school.manage.controller;

import com.school.manage.model.StudyMaterial;
import com.school.manage.service.StudyMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/study-materials")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<StudyMaterial> createMaterial(@RequestBody StudyMaterial material) {
        log.info("[StudyMaterialController] POST /api/study-materials — title='{}', class='{}'",
                material.getTitle(), material.getClassName());
        return new ResponseEntity<>(studyMaterialService.createMaterial(material), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<StudyMaterial>> getAllMaterials() {
        log.info("[StudyMaterialController] GET /api/study-materials");
        return ResponseEntity.ok(studyMaterialService.getAllMaterials());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyMaterial> getMaterialById(@PathVariable String id) {
        log.info("[StudyMaterialController] GET /api/study-materials/{}", id);
        return ResponseEntity.ok(studyMaterialService.getMaterialById(id));
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StudyMaterial>> getMaterialsByClass(@PathVariable String className) {
        log.info("[StudyMaterialController] GET /api/study-materials/class/{}", className);
        return ResponseEntity.ok(studyMaterialService.getMaterialsByClass(className));
    }

    @GetMapping("/class/{className}/subject/{subject}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StudyMaterial>> getMaterialsByClassAndSubject(
            @PathVariable String className, @PathVariable String subject) {
        log.info("[StudyMaterialController] GET /api/study-materials/class/{}/subject/{}", className, subject);
        return ResponseEntity.ok(studyMaterialService.getMaterialsByClassAndSubject(className, subject));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<StudyMaterial> updateMaterial(@PathVariable String id, @RequestBody StudyMaterial material) {
        log.info("[StudyMaterialController] PUT /api/study-materials/{}", id);
        return ResponseEntity.ok(studyMaterialService.updateMaterial(id, material));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable String id) {
        log.info("[StudyMaterialController] DELETE /api/study-materials/{}", id);
        studyMaterialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
