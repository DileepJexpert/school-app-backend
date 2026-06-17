package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Promotion;
import com.school.manage.model.Student;
import com.school.manage.repository.PromotionRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final StudentRepository studentRepository;

    public Promotion promoteStudent(Promotion promotion) {
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setPromotedAt(LocalDateTime.now());

        // Update the student document
        Student student = studentRepository.findById(promotion.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + promotion.getStudentId()));
        student.setClassForAdmission(promotion.getToClass());
        student.setAcademicYear(promotion.getToAcademicYear());
        studentRepository.save(student);

        log.info("[PromotionService] Promoted student='{}' from {} to {}",
                promotion.getStudentName(), promotion.getFromClass(), promotion.getToClass());
        return promotionRepository.save(promotion);
    }

    public List<Promotion> bulkPromote(String fromClass, String fromAcademicYear,
                                       String toClass, String toAcademicYear, String section) {
        List<Student> students = studentRepository
                .findByClassForAdmissionAndAcademicYear(fromClass, fromAcademicYear);

        List<Promotion> promotions = new ArrayList<>();
        for (Student student : students) {
            Promotion promotion = new Promotion();
            promotion.setStudentId(student.getId());
            promotion.setStudentName(student.getFullName());
            promotion.setFromClass(fromClass);
            promotion.setToClass(toClass);
            promotion.setFromAcademicYear(fromAcademicYear);
            promotion.setToAcademicYear(toAcademicYear);
            promotion.setToSection(section);
            promotion.setStatus("PROMOTED");
            promotion.setCreatedAt(LocalDateTime.now());
            promotion.setPromotedAt(LocalDateTime.now());

            // Update student record
            student.setClassForAdmission(toClass);
            student.setAcademicYear(toAcademicYear);
            studentRepository.save(student);

            promotions.add(promotionRepository.save(promotion));
        }

        log.info("[PromotionService] Bulk promoted {} students from {} ({}) to {} ({})",
                promotions.size(), fromClass, fromAcademicYear, toClass, toAcademicYear);
        return promotions;
    }

    public Promotion retainStudent(String studentId, String academicYear, String remarks) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Promotion promotion = new Promotion();
        promotion.setStudentId(studentId);
        promotion.setStudentName(student.getFullName());
        promotion.setFromClass(student.getClassForAdmission());
        promotion.setToClass(student.getClassForAdmission()); // stays in same class
        promotion.setFromAcademicYear(student.getAcademicYear());
        promotion.setToAcademicYear(academicYear);
        promotion.setStatus("RETAINED");
        promotion.setRemarks(remarks);
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setPromotedAt(LocalDateTime.now());

        // Update only academic year
        student.setAcademicYear(academicYear);
        studentRepository.save(student);

        log.info("[PromotionService] Retained student='{}' in class={}", student.getFullName(), student.getClassForAdmission());
        return promotionRepository.save(promotion);
    }

    public Promotion issueTC(String studentId, String remarks) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Promotion promotion = new Promotion();
        promotion.setStudentId(studentId);
        promotion.setStudentName(student.getFullName());
        promotion.setFromClass(student.getClassForAdmission());
        promotion.setFromAcademicYear(student.getAcademicYear());
        promotion.setStatus("TC_ISSUED");
        promotion.setRemarks(remarks);
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setPromotedAt(LocalDateTime.now());

        log.info("[PromotionService] TC issued for student='{}'", student.getFullName());
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getAll() {
        return promotionRepository.findAll();
    }

    public Promotion getById(String id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + id));
    }

    public List<Promotion> getByAcademicYear(String year) {
        return promotionRepository.findByFromAcademicYear(year);
    }

    public List<Promotion> getByClass(String className) {
        return promotionRepository.findByFromClass(className);
    }

    public List<Promotion> getByStudent(String studentId) {
        return promotionRepository.findByStudentId(studentId);
    }

    public Map<String, Object> getStats(String academicYear) {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalPromoted = promotionRepository.countByFromAcademicYearAndStatus(academicYear, "PROMOTED");
        long totalRetained = promotionRepository.countByFromAcademicYearAndStatus(academicYear, "RETAINED");
        long totalTC = promotionRepository.countByFromAcademicYearAndStatus(academicYear, "TC_ISSUED");

        stats.put("totalPromoted", totalPromoted);
        stats.put("totalRetained", totalRetained);
        stats.put("totalTC", totalTC);

        // Class-wise breakdown
        List<Promotion> yearPromotions = promotionRepository.findByFromAcademicYear(academicYear);
        Map<String, Map<String, Long>> classWiseBreakdown = yearPromotions.stream()
                .filter(p -> p.getFromClass() != null)
                .collect(Collectors.groupingBy(
                        Promotion::getFromClass,
                        Collectors.groupingBy(
                                p -> p.getStatus() != null ? p.getStatus() : "UNKNOWN",
                                Collectors.counting()
                        )
                ));
        stats.put("classWiseBreakdown", classWiseBreakdown);

        return stats;
    }
}
