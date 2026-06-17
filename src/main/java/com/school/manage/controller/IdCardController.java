package com.school.manage.controller;

import com.school.manage.service.IdCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/id-cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IdCardController {

    private final IdCardService idCardService;

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> generateStudentIdCard(@PathVariable String studentId) {
        log.info("Generating ID card PDF for student: {}", studentId);
        byte[] pdf = idCardService.generateStudentIdCard(studentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"id_card_" + studentId + ".pdf\"")
                .body(pdf);
    }

    @PostMapping("/students/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> generateBulkStudentIdCards(@RequestBody List<String> studentIds) {
        log.info("Generating bulk ID cards PDF for {} students", studentIds.size());
        byte[] pdf = idCardService.generateBulkIdCards(studentIds);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"id_cards_bulk.pdf\"")
                .body(pdf);
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<byte[]> generateStaffIdCard(@PathVariable String staffId) {
        log.info("Generating ID card PDF for staff: {}", staffId);
        byte[] pdf = idCardService.generateStaffIdCard(staffId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"id_card_staff_" + staffId + ".pdf\"")
                .body(pdf);
    }
}
