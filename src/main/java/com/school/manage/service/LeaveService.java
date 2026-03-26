package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.LeaveRequest;
import com.school.manage.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveRequest applyLeave(LeaveRequest request) {
        request.setStatus("PENDING");
        request.setAppliedAt(LocalDateTime.now());
        if (request.getFromDate() != null && request.getToDate() != null) {
            request.setTotalDays((int) ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1);
        }
        LeaveRequest saved = leaveRepository.save(request);
        log.info("[LeaveService] Leave applied: staffId='{}', type='{}', days={}",
                saved.getStaffId(), saved.getLeaveType(), saved.getTotalDays());
        return saved;
    }

    public LeaveRequest approveLeave(String leaveId, String approvedBy, String remarks, boolean approve) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveId));

        leave.setStatus(approve ? "APPROVED" : "REJECTED");
        leave.setApprovedBy(approvedBy);
        leave.setApproverRemarks(remarks);
        leave.setApprovedAt(LocalDateTime.now());

        log.info("[LeaveService] Leave {}: id='{}', staffId='{}'",
                approve ? "APPROVED" : "REJECTED", leaveId, leave.getStaffId());
        return leaveRepository.save(leave);
    }

    public List<LeaveRequest> getLeavesByStaff(String staffId) {
        return leaveRepository.findByStaffId(staffId);
    }

    public List<LeaveRequest> getLeavesByStatus(String status) {
        return leaveRepository.findByStatus(status);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public void cancelLeave(String leaveId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveId));
        leave.setStatus("CANCELLED");
        leaveRepository.save(leave);
    }
}
