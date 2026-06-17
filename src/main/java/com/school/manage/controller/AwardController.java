package com.school.manage.controller;

import com.school.manage.model.Award;
import com.school.manage.model.User;
import com.school.manage.service.AwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/awards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AwardController {

    private final AwardService awardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Award>> getAll() {
        log.info("[AwardController] GET /api/awards");
        return ResponseEntity.ok(awardService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Award> getById(@PathVariable String id) {
        log.info("[AwardController] GET /api/awards/{}", id);
        return ResponseEntity.ok(awardService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Award> create(@RequestBody Award award,
                                        Authentication auth) {
        User user = (User) auth.getPrincipal();
        award.setAddedBy(user.getFullName());
        log.info("[AwardController] POST /api/awards — by='{}', title='{}'",
                user.getFullName(), award.getTitle());
        return new ResponseEntity<>(awardService.create(award), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Award> update(@PathVariable String id,
                                        @RequestBody Award award) {
        log.info("[AwardController] PUT /api/awards/{}", id);
        return ResponseEntity.ok(awardService.update(id, award));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[AwardController] DELETE /api/awards/{}", id);
        awardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Award>> getByStudent(@PathVariable String studentId) {
        log.info("[AwardController] GET /api/awards/student/{}", studentId);
        return ResponseEntity.ok(awardService.getByStudent(studentId));
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Award>> getByClass(@PathVariable String className) {
        log.info("[AwardController] GET /api/awards/class/{}", className);
        return ResponseEntity.ok(awardService.getByClass(className));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Award>> getByCategory(@PathVariable String category) {
        log.info("[AwardController] GET /api/awards/category/{}", category);
        return ResponseEntity.ok(awardService.getByCategory(category));
    }

    @GetMapping("/level/{level}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Award>> getByLevel(@PathVariable String level) {
        log.info("[AwardController] GET /api/awards/level/{}", level);
        return ResponseEntity.ok(awardService.getByLevel(level));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[AwardController] GET /api/awards/stats");
        return ResponseEntity.ok(awardService.getStats());
    }
}
