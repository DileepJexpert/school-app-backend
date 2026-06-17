package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.HealthRecord;
import com.school.manage.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;

    public HealthRecord createOrUpdate(HealthRecord record) {
        Optional<HealthRecord> existing = healthRecordRepository.findByStudentId(record.getStudentId());
        if (existing.isPresent()) {
            HealthRecord ex = existing.get();
            record.setId(ex.getId());
            record.setCreatedAt(ex.getCreatedAt());
            record.setUpdatedAt(LocalDateTime.now());
            log.info("[HealthRecordService] Updating health record for studentId='{}'", record.getStudentId());
        } else {
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            log.info("[HealthRecordService] Creating health record for studentId='{}'", record.getStudentId());
        }
        return healthRecordRepository.save(record);
    }

    public Optional<HealthRecord> getByStudentId(String studentId) {
        return healthRecordRepository.findByStudentId(studentId);
    }

    public List<HealthRecord> getByClass(String className) {
        return healthRecordRepository.findByClassName(className);
    }

    public HealthRecord addMedicalVisit(String studentId, HealthRecord.MedicalVisit visit) {
        HealthRecord record = healthRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found for student: " + studentId));
        visit.setId(UUID.randomUUID().toString());
        if (record.getMedicalVisits() == null) {
            record.setMedicalVisits(new ArrayList<>());
        }
        record.getMedicalVisits().add(visit);
        record.setUpdatedAt(LocalDateTime.now());
        log.info("[HealthRecordService] Added medical visit for studentId='{}', visitId='{}'",
                studentId, visit.getId());
        return healthRecordRepository.save(record);
    }

    public HealthRecord removeMedicalVisit(String studentId, String visitId) {
        HealthRecord record = healthRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found for student: " + studentId));
        if (record.getMedicalVisits() != null) {
            record.getMedicalVisits().removeIf(v -> visitId.equals(v.getId()));
        }
        record.setUpdatedAt(LocalDateTime.now());
        log.info("[HealthRecordService] Removed medical visit '{}' for studentId='{}'", visitId, studentId);
        return healthRecordRepository.save(record);
    }

    public List<HealthRecord> getAll() {
        return healthRecordRepository.findAll();
    }

    public HealthRecord getById(String id) {
        return healthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found: " + id));
    }

    public void delete(String id) {
        healthRecordRepository.deleteById(id);
        log.info("[HealthRecordService] Deleted health record id='{}'", id);
    }

    public Map<String, Object> getStats() {
        List<HealthRecord> all = healthRecordRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", all.size());
        long allergiesCount = all.stream()
                .filter(r -> r.getAllergies() != null && !r.getAllergies().isEmpty())
                .count();
        stats.put("allergiesCount", allergiesCount);
        Map<String, Long> bloodGroupDistribution = all.stream()
                .filter(r -> r.getBloodGroup() != null && !r.getBloodGroup().isBlank())
                .collect(Collectors.groupingBy(HealthRecord::getBloodGroup, Collectors.counting()));
        stats.put("bloodGroupDistribution", bloodGroupDistribution);
        return stats;
    }
}
