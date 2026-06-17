package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Visitor;
import com.school.manage.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public Visitor create(Visitor visitor) {
        long todayCount = visitorRepository.countByVisitDate(LocalDate.now());
        visitor.setGatePassNumber(String.format("GP-%03d", todayCount + 1));
        visitor.setStatus("CHECKED_IN");
        visitor.setCheckInTime(LocalDateTime.now());
        visitor.setVisitDate(LocalDate.now());
        visitor.setCreatedAt(LocalDateTime.now());
        log.info("[VisitorService] Creating visitor: '{}', gate pass: {}",
                visitor.getVisitorName(), visitor.getGatePassNumber());
        return visitorRepository.save(visitor);
    }

    public Visitor checkOut(String id) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found: " + id));
        visitor.setStatus("CHECKED_OUT");
        visitor.setCheckOutTime(LocalDateTime.now());
        log.info("[VisitorService] Checking out visitor: '{}', gate pass: {}",
                visitor.getVisitorName(), visitor.getGatePassNumber());
        return visitorRepository.save(visitor);
    }

    public Visitor update(String id, Visitor updated) {
        Visitor existing = visitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found: " + id));
        existing.setVisitorName(updated.getVisitorName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setPurpose(updated.getPurpose());
        existing.setPurposeDetails(updated.getPurposeDetails());
        existing.setPersonToMeet(updated.getPersonToMeet());
        existing.setDepartment(updated.getDepartment());
        existing.setNumberOfVisitors(updated.getNumberOfVisitors());
        existing.setIdProofType(updated.getIdProofType());
        existing.setIdProofNumber(updated.getIdProofNumber());
        existing.setVehicleNumber(updated.getVehicleNumber());
        existing.setRemarks(updated.getRemarks());
        return visitorRepository.save(existing);
    }

    public void delete(String id) {
        visitorRepository.deleteById(id);
    }

    public List<Visitor> getAll() {
        return visitorRepository.findAll();
    }

    public Visitor getById(String id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found: " + id));
    }

    public List<Visitor> getByDate(LocalDate date) {
        return visitorRepository.findByVisitDate(date);
    }

    public List<Visitor> getToday() {
        return visitorRepository.findByVisitDate(LocalDate.now());
    }

    public List<Visitor> getCheckedIn() {
        return visitorRepository.findByStatus("CHECKED_IN");
    }

    public List<Visitor> search(String query) {
        List<Visitor> byName = visitorRepository.findByVisitorNameContainingIgnoreCase(query);
        List<Visitor> byPhone = visitorRepository.findByPhoneContaining(query);
        return Stream.concat(byName.stream(), byPhone.stream())
                .distinct()
                .toList();
    }

    public List<Visitor> getByDateRange(LocalDate from, LocalDate to) {
        return visitorRepository.findByVisitDateBetween(from, to);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        stats.put("todayCount", visitorRepository.countByVisitDate(today));
        stats.put("currentlyInBuilding", (long) visitorRepository.findByVisitDateAndStatus(today, "CHECKED_IN").size());
        stats.put("weekTotal", visitorRepository.findByVisitDateBetween(today.minusDays(7), today).size());
        return stats;
    }
}
