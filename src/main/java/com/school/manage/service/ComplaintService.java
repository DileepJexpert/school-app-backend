package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Complaint;
import com.school.manage.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public Complaint create(Complaint complaint) {
        long count = complaintRepository.count();
        complaint.setComplaintNumber(String.format("CMP-%03d", count + 1));
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        log.info("[ComplaintService] Creating complaint '{}' — number='{}', filedBy='{}'",
                complaint.getTitle(), complaint.getComplaintNumber(), complaint.getFilerName());
        return complaintRepository.save(complaint);
    }

    public Complaint update(String id, Complaint complaint) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        existing.setTitle(complaint.getTitle());
        existing.setDescription(complaint.getDescription());
        existing.setCategory(complaint.getCategory());
        existing.setPriority(complaint.getPriority());
        existing.setStudentId(complaint.getStudentId());
        existing.setStudentName(complaint.getStudentName());
        existing.setClassName(complaint.getClassName());
        existing.setResolution(complaint.getResolution());
        existing.setUpdatedAt(LocalDateTime.now());
        log.info("[ComplaintService] Updated complaint id='{}'", id);
        return complaintRepository.save(existing);
    }

    public void delete(String id) {
        complaintRepository.deleteById(id);
        log.info("[ComplaintService] Deleted complaint id='{}'", id);
    }

    public List<Complaint> getAll() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public Complaint getById(String id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
    }

    public List<Complaint> getByUser(String userId) {
        return complaintRepository.findByFiledBy(userId);
    }

    public List<Complaint> getByStatus(String status) {
        return complaintRepository.findByStatus(status);
    }

    public List<Complaint> getByCategory(String category) {
        return complaintRepository.findByCategory(category);
    }

    public List<Complaint> getByStudent(String studentId) {
        return complaintRepository.findByStudentId(studentId);
    }

    public Complaint updateStatus(String id, String status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        complaint.setStatus(status);
        if ("RESOLVED".equalsIgnoreCase(status)) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        complaint.setUpdatedAt(LocalDateTime.now());
        log.info("[ComplaintService] Status updated for complaint id='{}' to '{}'", id, status);
        return complaintRepository.save(complaint);
    }

    public Complaint assignTo(String id, String userId, String userName) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        complaint.setAssignedTo(userId);
        complaint.setAssignedToName(userName);
        complaint.setStatus("IN_PROGRESS");
        complaint.setUpdatedAt(LocalDateTime.now());
        log.info("[ComplaintService] Complaint id='{}' assigned to '{}'", id, userName);
        return complaintRepository.save(complaint);
    }

    public Complaint addComment(String id, Complaint.ComplaintComment comment) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        comment.setId(UUID.randomUUID().toString());
        comment.setCreatedAt(LocalDateTime.now());
        if (complaint.getComments() == null) {
            complaint.setComments(new ArrayList<>());
        }
        complaint.getComments().add(comment);
        complaint.setUpdatedAt(LocalDateTime.now());
        log.info("[ComplaintService] Comment added to complaint id='{}' by '{}'", id, comment.getUserName());
        return complaintRepository.save(complaint);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", complaintRepository.count());
        stats.put("openCount", complaintRepository.countByStatus("OPEN"));
        stats.put("inProgressCount", complaintRepository.countByStatus("IN_PROGRESS"));
        stats.put("resolvedCount", complaintRepository.countByStatus("RESOLVED"));
        stats.put("closedCount", complaintRepository.countByStatus("CLOSED"));

        List<Complaint> all = complaintRepository.findAll();
        Map<String, Long> categoryBreakdown = all.stream()
                .filter(c -> c.getCategory() != null && !c.getCategory().isBlank())
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));
        stats.put("categoryBreakdown", categoryBreakdown);

        return stats;
    }
}
