package com.school.manage.service;

import com.school.manage.dto.DisciplineSummaryDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Incident;
import com.school.manage.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisciplineService {

    private final IncidentRepository incidentRepository;

    public Incident createIncident(Incident incident) {
        incident.setCreatedAt(LocalDateTime.now());
        Incident saved = incidentRepository.save(incident);
        log.info("[DisciplineService] Incident created: id='{}', student='{}', severity='{}'",
                saved.getId(), saved.getStudentName(), saved.getSeverity());
        return saved;
    }

    public Incident updateIncident(String id, Incident details) {
        Incident existing = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        details.setId(existing.getId());
        details.setCreatedAt(existing.getCreatedAt());
        return incidentRepository.save(details);
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public List<Incident> getIncidentsByStudent(String studentId) {
        return incidentRepository.findByStudentId(studentId);
    }

    public List<Incident> getIncidentsByClass(String className, String academicYear) {
        return incidentRepository.findByClassNameAndAcademicYear(className, academicYear);
    }

    public List<Incident> getIncidentsByDateRange(LocalDate from, LocalDate to) {
        return incidentRepository.findByIncidentDateBetween(from, to);
    }

    public Incident resolveIncident(String id, String followUpNotes) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        incident.setResolved(true);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setFollowUpNotes(followUpNotes);
        return incidentRepository.save(incident);
    }

    public void deleteIncident(String id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident not found: " + id);
        }
        incidentRepository.deleteById(id);
    }

    public DisciplineSummaryDto getSummary() {
        List<Incident> all = incidentRepository.findAll();
        DisciplineSummaryDto dto = new DisciplineSummaryDto();
        dto.setTotalIncidents(all.size());
        dto.setResolvedIncidents(all.stream().filter(Incident::isResolved).count());
        dto.setUnresolvedIncidents(all.size() - dto.getResolvedIncidents());

        dto.setBySeverity(all.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getSeverity() != null ? i.getSeverity() : "UNKNOWN",
                        Collectors.counting())));

        dto.setByCategory(all.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCategory() != null ? i.getCategory() : "UNKNOWN",
                        Collectors.counting())));

        dto.setByClass(all.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getClassName() != null ? i.getClassName() : "UNKNOWN",
                        Collectors.counting())));

        return dto;
    }
}
