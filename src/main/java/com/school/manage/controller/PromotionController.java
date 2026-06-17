package com.school.manage.controller;

import com.school.manage.model.Promotion;
import com.school.manage.model.User;
import com.school.manage.service.PromotionService;
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
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Promotion>> getAll() {
        log.info("[PromotionController] GET /api/promotions");
        return ResponseEntity.ok(promotionService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Promotion> getById(@PathVariable String id) {
        log.info("[PromotionController] GET /api/promotions/{}", id);
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Promotion> promoteStudent(@RequestBody Promotion promotion,
                                                     Authentication auth) {
        User user = (User) auth.getPrincipal();
        promotion.setPromotedBy(user.getFullName());
        log.info("[PromotionController] POST /api/promotions — by='{}', student='{}'",
                user.getFullName(), promotion.getStudentName());
        return new ResponseEntity<>(promotionService.promoteStudent(promotion), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Promotion>> bulkPromote(@RequestBody Map<String, String> body,
                                                        Authentication auth) {
        User user = (User) auth.getPrincipal();
        String fromClass = body.get("fromClass");
        String fromAcademicYear = body.get("fromAcademicYear");
        String toClass = body.get("toClass");
        String toAcademicYear = body.get("toAcademicYear");
        String section = body.get("section");
        log.info("[PromotionController] POST /api/promotions/bulk — by='{}', from={} to={}",
                user.getFullName(), fromClass, toClass);
        return new ResponseEntity<>(
                promotionService.bulkPromote(fromClass, fromAcademicYear, toClass, toAcademicYear, section),
                HttpStatus.CREATED);
    }

    @PostMapping("/retain")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Promotion> retainStudent(@RequestBody Map<String, String> body,
                                                    Authentication auth) {
        User user = (User) auth.getPrincipal();
        String studentId = body.get("studentId");
        String academicYear = body.get("academicYear");
        String remarks = body.get("remarks");
        log.info("[PromotionController] POST /api/promotions/retain — by='{}', student='{}'",
                user.getFullName(), studentId);
        return new ResponseEntity<>(
                promotionService.retainStudent(studentId, academicYear, remarks),
                HttpStatus.CREATED);
    }

    @PostMapping("/tc")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Promotion> issueTC(@RequestBody Map<String, String> body,
                                              Authentication auth) {
        User user = (User) auth.getPrincipal();
        String studentId = body.get("studentId");
        String remarks = body.get("remarks");
        log.info("[PromotionController] POST /api/promotions/tc — by='{}', student='{}'",
                user.getFullName(), studentId);
        return new ResponseEntity<>(
                promotionService.issueTC(studentId, remarks),
                HttpStatus.CREATED);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Promotion>> getByYear(@PathVariable String academicYear) {
        log.info("[PromotionController] GET /api/promotions/year/{}", academicYear);
        return ResponseEntity.ok(promotionService.getByAcademicYear(academicYear));
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Promotion>> getByClass(@PathVariable String className) {
        log.info("[PromotionController] GET /api/promotions/class/{}", className);
        return ResponseEntity.ok(promotionService.getByClass(className));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Promotion>> getByStudent(@PathVariable String studentId) {
        log.info("[PromotionController] GET /api/promotions/student/{}", studentId);
        return ResponseEntity.ok(promotionService.getByStudent(studentId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam String academicYear) {
        log.info("[PromotionController] GET /api/promotions/stats?academicYear={}", academicYear);
        return ResponseEntity.ok(promotionService.getStats(academicYear));
    }
}
