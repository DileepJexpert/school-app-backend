package com.school.manage.controller;

import com.school.manage.dto.CertificateRequestDto;
import com.school.manage.model.CertificateRecord;
import com.school.manage.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<CertificateRecord> generateCertificate(
            @RequestBody CertificateRequestDto request,
            Authentication authentication) {
        String generatedBy = authentication.getName();
        log.info("Generating {} certificate for student: {} by {}",
                request.getCertificateType(), request.getStudentId(), generatedBy);
        return ResponseEntity.ok(certificateService.generateCertificate(request, generatedBy));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<CertificateRecord>> getStudentCertificates(
            @PathVariable String studentId) {
        return ResponseEntity.ok(certificateService.getHistory(studentId));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<CertificateRecord>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(certificateService.getByType(type));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<CertificateRecord>> getAllCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertificates());
    }
}
