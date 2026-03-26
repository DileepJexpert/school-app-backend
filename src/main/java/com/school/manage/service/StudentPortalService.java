package com.school.manage.service;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ChildOverviewDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.*;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentPortalService {

    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final ResultService resultService;
    private final FeeService feeService;

    /**
     * Build student's own dashboard using their linkedEntityId.
     */
    public ChildOverviewDto getDashboard(String studentEntityId) {
        log.info("[StudentPortalService] Building dashboard for student entity: {}", studentEntityId);

        Student student = studentRepository.findById(studentEntityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + studentEntityId));

        ChildOverviewDto dto = new ChildOverviewDto();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getFullName());
        dto.setClassName(student.getClassForAdmission());
        dto.setRollNumber(student.getRollNumber());
        dto.setAdmissionNumber(student.getAdmissionNumber());

        // Attendance
        try {
            AttendanceSummaryDto summary = attendanceService.getAttendanceSummary(
                    student.getId(), student.getAcademicYear());
            dto.setAttendancePercentage(summary.getAttendancePercentage());
            dto.setTotalPresent(summary.getPresentDays());
            dto.setTotalAbsent(summary.getAbsentDays());
        } catch (Exception e) {
            log.debug("No attendance data for student {}", student.getId());
        }

        // Fees
        try {
            var feeProfile = feeService.getStudentFeeProfile(student.getId());
            dto.setTotalFees(feeProfile.getTotalFee());
            dto.setPaidFees(feeProfile.getPaidAmount());
            dto.setPendingFees(feeProfile.getTotalFee() - feeProfile.getPaidAmount());
        } catch (Exception e) {
            log.debug("No fee profile for student {}", student.getId());
        }

        return dto;
    }

    public List<Attendance> getMyAttendance(String studentEntityId, LocalDate from, LocalDate to) {
        return attendanceService.getAttendanceByStudentAndDateRange(studentEntityId, from, to);
    }

    public AttendanceSummaryDto getMyAttendanceSummary(String studentEntityId, String academicYear) {
        return attendanceService.getAttendanceSummary(studentEntityId, academicYear);
    }
}
