package com.school.manage.service;

import com.school.manage.dto.CertificateRequestDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.CertificateRecord;
import com.school.manage.model.Student;
import com.school.manage.repository.CertificateRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;

    /**
     * Generate a certificate record and return it.
     * Actual PDF generation is done in the controller using the record data.
     */
    public CertificateRecord generateCertificate(CertificateRequestDto request, String generatedBy) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + request.getStudentId()));

        CertificateRecord record = new CertificateRecord();
        record.setStudentId(student.getId());
        record.setStudentName(student.getFullName());
        record.setClassName(student.getClassForAdmission());
        record.setAcademicYear(student.getAcademicYear());
        record.setCertificateType(request.getCertificateType());
        record.setReason(request.getReason());
        record.setAdditionalFields(request.getAdditionalFields());
        record.setGeneratedBy(generatedBy);
        record.setGeneratedAt(LocalDateTime.now());
        record.setSerialNumber(generateSerialNumber(request.getCertificateType()));

        CertificateRecord saved = certificateRepository.save(record);
        log.info("[CertificateService] Certificate generated: type='{}', student='{}', serial='{}'",
                saved.getCertificateType(), saved.getStudentName(), saved.getSerialNumber());
        return saved;
    }

    public List<CertificateRecord> getHistory(String studentId) {
        return certificateRepository.findByStudentId(studentId);
    }

    public List<CertificateRecord> getByType(String type) {
        return certificateRepository.findByCertificateType(type);
    }

    public List<CertificateRecord> getByClassAndYear(String className, String academicYear) {
        return certificateRepository.findByClassNameAndAcademicYear(className, academicYear);
    }

    private String generateSerialNumber(String type) {
        long count = certificateRepository.count();
        String prefix = switch (type) {
            case "TRANSFER" -> "TC";
            case "BONAFIDE" -> "BF";
            case "CHARACTER" -> "CC";
            case "STUDY" -> "SC";
            case "ID_CARD" -> "ID";
            default -> "CERT";
        };
        return prefix + "-" + (count + 1001);
    }
}
