package com.school.manage.controller;

import com.school.manage.model.LeaveRequest;
import com.school.manage.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','TRANSPORT_MANAGER')")
    public ResponseEntity<LeaveRequest> applyLeave(@RequestBody LeaveRequest request) {
        log.info("[LeaveController] POST /api/leave/apply");
        return new ResponseEntity<>(leaveService.applyLeave(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String approvedBy = body.getOrDefault("approvedBy", "Admin");
        String remarks = body.getOrDefault("remarks", "");
        String action = body.getOrDefault("action", "approve");
        boolean approve = !"reject".equalsIgnoreCase(action);
        return ResponseEntity.ok(leaveService.approveLeave(id, approvedBy, remarks, approve));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<LeaveRequest>> getAllLeaves(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(leaveService.getLeavesByStatus(status));
        }
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','ACCOUNTANT','TRANSPORT_MANAGER')")
    public ResponseEntity<List<LeaveRequest>> getLeavesByStaff(@PathVariable String staffId) {
        return ResponseEntity.ok(leaveService.getLeavesByStaff(staffId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> cancelLeave(@PathVariable String id) {
        leaveService.cancelLeave(id);
        return ResponseEntity.noContent().build();
    }
}
