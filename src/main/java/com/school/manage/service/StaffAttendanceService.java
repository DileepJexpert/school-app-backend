package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.StaffAttendance;
import com.school.manage.repository.StaffAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;

    public StaffAttendance markAttendance(StaffAttendance attendance) {
        attendance.setCreatedAt(LocalDateTime.now());
        return staffAttendanceRepository.save(attendance);
    }

    public List<StaffAttendance> markBulkAttendance(List<StaffAttendance> attendanceList) {
        attendanceList.forEach(a -> a.setCreatedAt(LocalDateTime.now()));
        return staffAttendanceRepository.saveAll(attendanceList);
    }

    public StaffAttendance updateAttendance(String id, StaffAttendance attendance) {
        StaffAttendance existing = staffAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff attendance not found with id: " + id));
        attendance.setId(existing.getId());
        attendance.setCreatedAt(existing.getCreatedAt());
        return staffAttendanceRepository.save(attendance);
    }

    public List<StaffAttendance> getByDate(LocalDate date) {
        return staffAttendanceRepository.findByDate(date);
    }

    public List<StaffAttendance> getByStaff(String userId) {
        return staffAttendanceRepository.findByUserId(userId);
    }

    public List<StaffAttendance> getByStaffAndDateRange(String userId, LocalDate from, LocalDate to) {
        return staffAttendanceRepository.findByUserIdAndDateBetween(userId, from, to);
    }

    public List<StaffAttendance> getByDateRange(LocalDate from, LocalDate to) {
        return staffAttendanceRepository.findByDateBetween(from, to);
    }

    public Map<String, Object> getTodayStats() {
        LocalDate today = LocalDate.now();
        long presentCount = staffAttendanceRepository.countByDateAndStatus(today, "PRESENT");
        long absentCount = staffAttendanceRepository.countByDateAndStatus(today, "ABSENT");
        long halfDayCount = staffAttendanceRepository.countByDateAndStatus(today, "HALF_DAY");
        long onLeaveCount = staffAttendanceRepository.countByDateAndStatus(today, "ON_LEAVE");
        long lateCount = staffAttendanceRepository.countByDateAndStatus(today, "LATE");
        long total = presentCount + absentCount + halfDayCount + onLeaveCount + lateCount;

        Map<String, Object> stats = new HashMap<>();
        stats.put("presentCount", presentCount);
        stats.put("absentCount", absentCount);
        stats.put("halfDayCount", halfDayCount);
        stats.put("onLeaveCount", onLeaveCount);
        stats.put("lateCount", lateCount);
        stats.put("total", total);
        return stats;
    }
}
