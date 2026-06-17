package com.school.manage.controller;

import com.school.manage.dto.AttendanceSummaryDto;
import com.school.manage.dto.ParentDashboardDto;
import com.school.manage.model.Attendance;
import com.school.manage.model.Award;
import com.school.manage.model.Complaint;
import com.school.manage.model.DailyDiary;
import com.school.manage.model.ExamSchedule;
import com.school.manage.model.HealthRecord;
import com.school.manage.model.Meeting;
import com.school.manage.model.Notice;
import com.school.manage.model.Student;
import com.school.manage.model.User;
import com.school.manage.repository.StudentRepository;
import com.school.manage.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;
    private final AttendanceService attendanceService;
    private final ResultService resultService;
    private final ReportCardPdfService reportCardPdfService;
    private final FeeService feeService;
    private final ExamScheduleService examScheduleService;
    private final StudentRepository studentRepository;
    private final DailyDiaryService dailyDiaryService;
    private final HealthRecordService healthRecordService;
    private final ComplaintService complaintService;
    private final NoticeService noticeService;
    private final AwardService awardService;
    private final MeetingService meetingService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDashboardDto> getDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] GET /api/parent/dashboard");
        return ResponseEntity.ok(parentPortalService.getDashboard(user));
    }

    @GetMapping("/child/{studentId}/attendance")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Attendance>> getChildAttendance(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(attendanceService.getAttendanceByStudentAndDateRange(studentId, from, to));
    }

    @GetMapping("/child/{studentId}/attendance/summary")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<AttendanceSummaryDto> getChildAttendanceSummary(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(studentId, academicYear));
    }

    @GetMapping("/child/{studentId}/results")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getChildResults(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(resultService.getStudentReportCard(studentId, academicYear));
    }

    @GetMapping("/child/{studentId}/results/pdf")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<byte[]> downloadChildReportCard(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        byte[] pdf = reportCardPdfService.generateReportCardPdf(studentId, academicYear);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_card.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/child/{studentId}/fees")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getChildFees(
            Authentication auth,
            @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        return ResponseEntity.ok(feeService.getStudentFeeProfile(studentId));
    }

    @GetMapping("/child/{studentId}/exam-schedule")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ExamSchedule>> getChildExamSchedule(
            Authentication auth,
            @PathVariable String studentId,
            @RequestParam(required = false) String academicYear) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        log.info("[ParentPortalController] GET /api/parent/child/{}/exam-schedule", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        String year = (academicYear != null && !academicYear.isBlank()) ? academicYear : student.getAcademicYear();
        return ResponseEntity.ok(examScheduleService.getPublishedSchedules(student.getClassForAdmission(), year));
    }

    @GetMapping("/child/{studentId}/daily-diary")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<DailyDiary>> getChildDailyDiary(
            Authentication auth,
            @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        log.info("[ParentPortalController] GET /api/parent/child/{}/daily-diary", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        return ResponseEntity.ok(dailyDiaryService.getByClass(student.getClassForAdmission()));
    }

    @GetMapping("/child/{studentId}/daily-diary/today")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<DailyDiary>> getChildDailyDiaryToday(
            Authentication auth,
            @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        log.info("[ParentPortalController] GET /api/parent/child/{}/daily-diary/today", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        return ResponseEntity.ok(dailyDiaryService.getByClassAndDate(
                student.getClassForAdmission(), LocalDate.now()));
    }

    // ─── Health Record ────────────────────────────────────────────────────

    @GetMapping("/child/{studentId}/health-record")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<HealthRecord> getChildHealthRecord(
            Authentication auth,
            @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        log.info("[ParentPortalController] GET /api/parent/child/{}/health-record", studentId);
        return ResponseEntity.ok(healthRecordService.getByStudentId(studentId)
                .orElse(null));
    }

    // ─── Complaints ─────────────────────────────────────────────────────

    @GetMapping("/complaints")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Complaint>> getMyComplaints(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] GET /api/parent/complaints for user={}", user.getId());
        return ResponseEntity.ok(complaintService.getByUser(user.getId()));
    }

    @PostMapping("/complaints")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Complaint> fileComplaint(@RequestBody Complaint complaint,
                                                   Authentication auth) {
        User user = (User) auth.getPrincipal();
        complaint.setFiledBy(user.getId());
        complaint.setFilerName(user.getFullName());
        complaint.setFilerRole("PARENT");
        log.info("[ParentPortalController] POST /api/parent/complaints — user='{}', title='{}'",
                user.getFullName(), complaint.getTitle());
        return new ResponseEntity<>(complaintService.create(complaint), HttpStatus.CREATED);
    }

    // ─── Notices ─────────────────────────────────────────────────────────

    @GetMapping("/notices")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Notice>> getNotices(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] GET /api/parent/notices — user='{}'", user.getId());

        List<Notice> published = noticeService.getPublished();
        List<Notice> filtered = new ArrayList<>();
        for (Notice n : published) {
            String audience = n.getTargetAudience();
            if ("ALL".equals(audience) || "PARENTS".equals(audience)) {
                filtered.add(n);
            }
        }
        return ResponseEntity.ok(filtered);
    }

    // ─── Awards ──────────────────────────────────────────────────────────

    @GetMapping("/child/{studentId}/awards")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Award>> getChildAwards(Authentication auth,
                                                      @PathVariable String studentId) {
        User user = (User) auth.getPrincipal();
        parentPortalService.validateParentAccess(user, studentId);
        log.info("[ParentPortalController] GET /api/parent/child/{}/awards", studentId);
        return ResponseEntity.ok(awardService.getByStudent(studentId));
    }

    // ─── Meetings ───────────────────────────────────────────────────────

    @GetMapping("/meetings")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Meeting>> getMyMeetings(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] GET /api/parent/meetings — user='{}'", user.getId());
        return ResponseEntity.ok(meetingService.getByParent(user.getId()));
    }

    @PostMapping("/meetings")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Meeting> bookMeeting(@RequestBody Meeting meeting,
                                                Authentication auth) {
        User user = (User) auth.getPrincipal();
        meeting.setParentId(user.getId());
        meeting.setParentName(user.getFullName());
        meeting.setMeetingType("PARENT_TEACHER");
        meeting.setCreatedBy(user.getFullName());
        meeting.setCreatedByRole("PARENT");
        log.info("[ParentPortalController] POST /api/parent/meetings — parent='{}', teacher='{}'",
                user.getFullName(), meeting.getTeacherId());
        return new ResponseEntity<>(meetingService.create(meeting), HttpStatus.CREATED);
    }

    @PutMapping("/meetings/{meetingId}/feedback")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Meeting> addMeetingFeedback(@PathVariable String meetingId,
                                                       @RequestBody Map<String, String> body,
                                                       Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[ParentPortalController] PUT /api/parent/meetings/{}/feedback — parent='{}'",
                meetingId, user.getFullName());
        return ResponseEntity.ok(meetingService.addFeedback(meetingId, body.get("feedback")));
    }
}
