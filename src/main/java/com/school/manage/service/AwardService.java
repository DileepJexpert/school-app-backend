package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Award;
import com.school.manage.repository.AwardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwardService {

    private final AwardRepository awardRepository;

    public Award create(Award award) {
        award.setCreatedAt(LocalDateTime.now());
        log.info("[AwardService] Creating award title='{}' for student='{}'",
                award.getTitle(), award.getStudentName());
        return awardRepository.save(award);
    }

    public Award update(String id, Award award) {
        Award existing = awardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Award not found: " + id));
        existing.setStudentId(award.getStudentId());
        existing.setStudentName(award.getStudentName());
        existing.setClassName(award.getClassName());
        existing.setTitle(award.getTitle());
        existing.setDescription(award.getDescription());
        existing.setCategory(award.getCategory());
        existing.setLevel(award.getLevel());
        existing.setPosition(award.getPosition());
        existing.setDateAwarded(award.getDateAwarded());
        existing.setAwardedBy(award.getAwardedBy());
        existing.setCertificateUrl(award.getCertificateUrl());
        existing.setAcademicYear(award.getAcademicYear());
        log.info("[AwardService] Updated award id='{}'", id);
        return awardRepository.save(existing);
    }

    public void delete(String id) {
        awardRepository.deleteById(id);
        log.info("[AwardService] Deleted award id='{}'", id);
    }

    public List<Award> getAll() {
        return awardRepository.findAllByOrderByDateAwardedDesc();
    }

    public Award getById(String id) {
        return awardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Award not found: " + id));
    }

    public List<Award> getByStudent(String studentId) {
        return awardRepository.findByStudentId(studentId);
    }

    public List<Award> getByClass(String className) {
        return awardRepository.findByClassName(className);
    }

    public List<Award> getByCategory(String category) {
        return awardRepository.findByCategory(category);
    }

    public List<Award> getByLevel(String level) {
        return awardRepository.findByLevel(level);
    }

    public List<Award> getByAcademicYear(String year) {
        return awardRepository.findByAcademicYear(year);
    }

    public List<Award> getStudentAwards(String studentId, String academicYear) {
        return awardRepository.findByStudentIdAndAcademicYear(studentId, academicYear);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Award> all = awardRepository.findAll();
        stats.put("totalAwards", (long) all.size());

        Map<String, Long> categoryBreakdown = all.stream()
                .filter(a -> a.getCategory() != null && !a.getCategory().isBlank())
                .collect(Collectors.groupingBy(Award::getCategory, Collectors.counting()));
        stats.put("categoryBreakdown", categoryBreakdown);

        Map<String, Long> levelBreakdown = all.stream()
                .filter(a -> a.getLevel() != null && !a.getLevel().isBlank())
                .collect(Collectors.groupingBy(Award::getLevel, Collectors.counting()));
        stats.put("levelBreakdown", levelBreakdown);

        // Top 5 students with most awards
        Map<String, Long> studentCounts = all.stream()
                .filter(a -> a.getStudentId() != null)
                .collect(Collectors.groupingBy(Award::getStudentId, Collectors.counting()));

        List<Map<String, Object>> topStudents = studentCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> studentInfo = new LinkedHashMap<>();
                    studentInfo.put("studentId", entry.getKey());
                    // Find the student's name and class from any of their awards
                    all.stream()
                            .filter(a -> entry.getKey().equals(a.getStudentId()))
                            .findFirst()
                            .ifPresent(a -> {
                                studentInfo.put("studentName", a.getStudentName());
                                studentInfo.put("className", a.getClassName());
                            });
                    studentInfo.put("awardCount", entry.getValue());
                    return studentInfo;
                })
                .collect(Collectors.toList());
        stats.put("topStudents", topStudents);

        return stats;
    }
}
