package com.school.manage.service;

import com.school.manage.dto.AttendanceRequestDto;
import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Attendance;
import com.school.manage.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    /**
     * Marks attendance for multiple students in a class for a given date.
     * If a record already exists for a student on that date, it will be updated.
     */
    public List<Attendance> markBulkAttendance(AttendanceRequestDto request) {
        List<Attendance> savedRecords = new ArrayList<>();

        for (AttendanceRequestDto.StudentAttendanceEntry entry : request.getEntries()) {
            // Check if attendance already marked for this student on this date
            Attendance attendance = attendanceRepository
                    .findByStudentIdAndDate(entry.getStudentId(), request.getDate())
                    .orElse(new Attendance());

            attendance.setStudentId(entry.getStudentId());
            attendance.setStudentName(entry.getStudentName());
            attendance.setClassName(request.getClassName());
            attendance.setAcademicYear(request.getAcademicYear());
            attendance.setDate(request.getDate());
            attendance.setStatus(entry.getStatus());
            attendance.setRemarks(entry.getRemarks());
            attendance.setMarkedBy(request.getMarkedBy());
            attendance.setMarkedAt(LocalDateTime.now());

            savedRecords.add(attendanceRepository.save(attendance));
        }

        return savedRecords;
    }

    /**
     * Returns attendance records for a specific student between two dates.
     */
    public List<Attendance> getAttendanceByStudentAndDateRange(
            String studentId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByStudentIdAndDateBetween(studentId, from, to);
    }

    /**
     * Returns all attendance records for a class on a specific date.
     */
    public List<Attendance> getAttendanceByClassAndDate(String className, LocalDate date) {
        return attendanceRepository.findByClassNameAndDate(className, date);
    }

    /**
     * Returns a summary of attendance for a student in an academic year.
     */
    public AttendanceSummaryDto getAttendanceSummary(String studentId, String academicYear) {
        List<Attendance> records = attendanceRepository
                .findByStudentIdAndAcademicYear(studentId, academicYear);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No attendance records found for student: " + studentId);
        }

        AttendanceSummaryDto summary = new AttendanceSummaryDto();
        summary.setStudentId(studentId);
        summary.setStudentName(records.get(0).getStudentName());
        summary.setClassName(records.get(0).getClassName());
        summary.setAcademicYear(academicYear);
        summary.setTotalDays(records.size());
        summary.setPresentDays(records.stream().filter(a -> "PRESENT".equals(a.getStatus())).count());
        summary.setAbsentDays(records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count());
        summary.setLateDays(records.stream().filter(a -> "LATE".equals(a.getStatus())).count());
        summary.setHalfDays(records.stream().filter(a -> "HALF_DAY".equals(a.getStatus())).count());

        double percentage = records.isEmpty() ? 0 :
                ((double) (summary.getPresentDays() + summary.getLateDays()) / summary.getTotalDays()) * 100;
        summary.setAttendancePercentage(Math.round(percentage * 100.0) / 100.0);

        return summary;
    }

    /**
     * Returns all attendance records for a class within a date range.
     */
    public List<Attendance> getAttendanceByClassAndDateRange(
            String className, String academicYear, LocalDate from, LocalDate to) {
        return attendanceRepository.findByClassNameAndAcademicYearAndDateBetween(
                className, academicYear, from, to);
    }

    /**
     * Returns a single attendance record by ID.
     */
    public Attendance getAttendanceById(String id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));
    }

    /**
     * Deletes an attendance record by ID.
     */
    public void deleteAttendance(String id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance record not found: " + id);
        }
        attendanceRepository.deleteById(id);
    }
}
