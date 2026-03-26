package com.school.manage.service;

import com.school.manage.dto.StaffDashboardDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Staff;
import com.school.manage.repository.LeaveRepository;
import com.school.manage.repository.SalaryRecordRepository;
import com.school.manage.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final LeaveRepository leaveRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    public Staff createStaff(Staff staff) {
        if (staff.getEmployeeId() == null || staff.getEmployeeId().isBlank()) {
            staff.setEmployeeId("EMP-" + (staffRepository.count() + 1001));
        }
        staff.setCreatedAt(LocalDateTime.now());
        Staff saved = staffRepository.save(staff);
        log.info("[StaffService] Created staff: id='{}', employeeId='{}'", saved.getId(), saved.getEmployeeId());
        return saved;
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(String id) {
        return staffRepository.findById(id);
    }

    public List<Staff> getStaffByDepartment(String department) {
        return staffRepository.findByDepartment(department);
    }

    public List<Staff> searchStaff(String name) {
        return staffRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Staff updateStaff(String id, Staff staffDetails) {
        Staff existing = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        staffDetails.setId(existing.getId());
        staffDetails.setEmployeeId(existing.getEmployeeId());
        staffDetails.setCreatedAt(existing.getCreatedAt());
        staffDetails.setUpdatedAt(LocalDateTime.now());
        return staffRepository.save(staffDetails);
    }

    public void deleteStaff(String id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff not found: " + id);
        }
        staffRepository.deleteById(id);
    }

    public StaffDashboardDto getDashboard() {
        StaffDashboardDto dto = new StaffDashboardDto();
        dto.setTotalStaff(staffRepository.count());
        dto.setActiveStaff(staffRepository.countByStatus("ACTIVE"));
        dto.setOnLeaveToday(staffRepository.countByStatus("ON_LEAVE"));
        dto.setPendingLeaveRequests(leaveRepository.countByStatus("PENDING"));

        Map<String, Long> deptWise = new HashMap<>();
        for (String dept : List.of("TEACHING", "ADMINISTRATION", "ACCOUNTS", "TRANSPORT", "SUPPORT")) {
            long count = staffRepository.countByDepartment(dept);
            if (count > 0) deptWise.put(dept, count);
        }
        dto.setDepartmentWise(deptWise);

        // Calculate total monthly payroll from active staff
        List<Staff> activeStaff = staffRepository.findByStatus("ACTIVE");
        dto.setTotalMonthlyPayroll(activeStaff.stream().mapToDouble(Staff::getBasicSalary).sum());

        return dto;
    }
}
