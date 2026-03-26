package com.school.manage.service;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ChildOverviewDto;
import com.school.manage.dto.ParentDashboardDto;
import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.*;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentPortalService {

    private final StudentRepository studentRepository;
    private final AttendanceService attendanceService;
    private final ResultService resultService;
    private final FeeService feeService;
    private final MongoTemplate mongoTemplate;

    public ParentDashboardDto getDashboard(User parentUser) {
        log.info("[ParentPortalService] Building dashboard for parent: {}", parentUser.getEmail());

        ParentDashboardDto dashboard = new ParentDashboardDto();
        dashboard.setParentName(parentUser.getFullName());
        dashboard.setParentEmail(parentUser.getEmail());

        List<String> childIds = parseLinkedIds(parentUser.getLinkedEntityId());
        List<ChildOverviewDto> children = new ArrayList<>();

        for (String studentId : childIds) {
            studentRepository.findById(studentId).ifPresent(student -> {
                ChildOverviewDto child = buildChildOverview(student);
                children.add(child);
            });
        }

        dashboard.setChildren(children);
        log.info("[ParentPortalService] Dashboard built with {} children", children.size());
        return dashboard;
    }

    public void validateParentAccess(User parentUser, String studentId) {
        List<String> childIds = parseLinkedIds(parentUser.getLinkedEntityId());
        if (!childIds.contains(studentId)) {
            log.warn("[ParentPortalService] Access denied: parent '{}' tried to access student '{}'",
                    parentUser.getId(), studentId);
            throw new SecurityException("You do not have access to this student's data");
        }
    }

    private ChildOverviewDto buildChildOverview(Student student) {
        ChildOverviewDto dto = new ChildOverviewDto();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getFullName());
        dto.setClassName(student.getClassForAdmission());
        dto.setRollNumber(student.getRollNumber());
        dto.setAdmissionNumber(student.getAdmissionNumber());

        // Attendance summary
        try {
            AttendanceSummaryDto attendance = attendanceService.getAttendanceSummary(
                    student.getId(), student.getAcademicYear());
            dto.setAttendancePercentage(attendance.getAttendancePercentage());
            dto.setTotalPresent(attendance.getPresentDays());
            dto.setTotalAbsent(attendance.getAbsentDays());
        } catch (Exception e) {
            log.debug("No attendance data for student {}", student.getId());
        }

        // Fee summary
        try {
            var feeProfile = feeService.getStudentFeeProfile(student.getId());
            dto.setTotalFees(feeProfile.getTotalFees().doubleValue());
            dto.setPaidFees(feeProfile.getPaidFees().doubleValue());
            dto.setPendingFees(feeProfile.getDueFees().doubleValue());
        } catch (Exception e) {
            log.debug("No fee profile for student {}", student.getId());
        }

        return dto;
    }

    private List<String> parseLinkedIds(String linkedEntityId) {
        if (linkedEntityId == null || linkedEntityId.isBlank()) {
            return List.of();
        }
        return List.of(linkedEntityId.split(","));
    }
}
