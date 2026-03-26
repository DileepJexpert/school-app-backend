package com.school.manage.service;

import com.school.manage.model.StaffAttendance;
import com.school.manage.repository.StaffAttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;

    /**
     * Mark or update attendance for multiple staff on a given date.
     */
    public List<StaffAttendance> markBulkAttendance(List<StaffAttendance> entries) {
        List<StaffAttendance> saved = new ArrayList<>();
        for (StaffAttendance entry : entries) {
            StaffAttendance attendance = staffAttendanceRepository
                    .findByStaffIdAndDate(entry.getStaffId(), entry.getDate())
                    .orElse(new StaffAttendance());

            attendance.setStaffId(entry.getStaffId());
            attendance.setStaffName(entry.getStaffName());
            attendance.setDepartment(entry.getDepartment());
            attendance.setDate(entry.getDate());
            attendance.setStatus(entry.getStatus());
            attendance.setCheckInTime(entry.getCheckInTime());
            attendance.setCheckOutTime(entry.getCheckOutTime());
            attendance.setRemarks(entry.getRemarks());
            attendance.setMarkedBy(entry.getMarkedBy());
            attendance.setMarkedAt(LocalDateTime.now());

            saved.add(staffAttendanceRepository.save(attendance));
        }
        log.info("[StaffAttendanceService] Marked attendance for {} staff", saved.size());
        return saved;
    }

    public List<StaffAttendance> getAttendanceByDate(LocalDate date) {
        return staffAttendanceRepository.findByDate(date);
    }

    public List<StaffAttendance> getAttendanceByStaff(String staffId, LocalDate from, LocalDate to) {
        return staffAttendanceRepository.findByStaffIdAndDateBetween(staffId, from, to);
    }

    public List<StaffAttendance> getAttendanceByDepartment(String department, LocalDate date) {
        return staffAttendanceRepository.findByDepartmentAndDate(department, date);
    }
}
