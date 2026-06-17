package com.school.manage.controller;

import com.school.manage.model.Complaint;
import com.school.manage.model.User;
import com.school.manage.service.ComplaintService;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Complaint>> getAll() {
        log.info("[ComplaintController] GET /api/complaints");
        return ResponseEntity.ok(complaintService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Complaint> getById(@PathVariable String id) {
        log.info("[ComplaintController] GET /api/complaints/{}", id);
        return ResponseEntity.ok(complaintService.getById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Complaint> create(@RequestBody Complaint complaint,
                                            Authentication auth) {
        User user = (User) auth.getPrincipal();
        complaint.setFiledBy(user.getId());
        complaint.setFilerName(user.getFullName());
        complaint.setFilerRole(user.getRole().name());
        log.info("[ComplaintController] POST /api/complaints — filer='{}', title='{}'",
                user.getFullName(), complaint.getTitle());
        return new ResponseEntity<>(complaintService.create(complaint), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Complaint> update(@PathVariable String id,
                                            @RequestBody Complaint complaint) {
        log.info("[ComplaintController] PUT /api/complaints/{}", id);
        return ResponseEntity.ok(complaintService.update(id, complaint));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[ComplaintController] DELETE /api/complaints/{}", id);
        complaintService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Complaint>> getMyComplaints(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ComplaintController] GET /api/complaints/my — user='{}'", user.getId());
        return ResponseEntity.ok(complaintService.getByUser(user.getId()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Complaint>> getByStatus(@PathVariable String status) {
        log.info("[ComplaintController] GET /api/complaints/status/{}", status);
        return ResponseEntity.ok(complaintService.getByStatus(status));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Complaint>> getByCategory(@PathVariable String category) {
        log.info("[ComplaintController] GET /api/complaints/category/{}", category);
        return ResponseEntity.ok(complaintService.getByCategory(category));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Complaint> updateStatus(@PathVariable String id,
                                                  @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("[ComplaintController] PUT /api/complaints/{}/status — status='{}'", id, status);
        return ResponseEntity.ok(complaintService.updateStatus(id, status));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Complaint> assign(@PathVariable String id,
                                            @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String userName = body.get("userName");
        log.info("[ComplaintController] PUT /api/complaints/{}/assign — assignedTo='{}'", id, userName);
        return ResponseEntity.ok(complaintService.assignTo(id, userId, userName));
    }

    @PostMapping("/{id}/comment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Complaint> addComment(@PathVariable String id,
                                                @RequestBody Complaint.ComplaintComment comment,
                                                Authentication auth) {
        User user = (User) auth.getPrincipal();
        comment.setUserId(user.getId());
        comment.setUserName(user.getFullName());
        comment.setUserRole(user.getRole().name());
        log.info("[ComplaintController] POST /api/complaints/{}/comment — by='{}'", id, user.getFullName());
        return ResponseEntity.ok(complaintService.addComment(id, comment));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[ComplaintController] GET /api/complaints/stats");
        return ResponseEntity.ok(complaintService.getStats());
    }
}
