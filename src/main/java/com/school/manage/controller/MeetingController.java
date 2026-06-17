package com.school.manage.controller;

import com.school.manage.model.Meeting;
import com.school.manage.model.User;
import com.school.manage.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Meeting>> getAll() {
        log.info("[MeetingController] GET /api/meetings");
        return ResponseEntity.ok(meetingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> getById(@PathVariable String id) {
        log.info("[MeetingController] GET /api/meetings/{}", id);
        return ResponseEntity.ok(meetingService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> create(@RequestBody Meeting meeting,
                                          Authentication auth) {
        User user = (User) auth.getPrincipal();
        meeting.setCreatedBy(user.getFullName());
        meeting.setCreatedByRole(user.getRole().name());
        log.info("[MeetingController] POST /api/meetings — by='{}', title='{}'",
                user.getFullName(), meeting.getTitle());
        return new ResponseEntity<>(meetingService.create(meeting), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> update(@PathVariable String id,
                                          @RequestBody Meeting meeting) {
        log.info("[MeetingController] PUT /api/meetings/{}", id);
        return ResponseEntity.ok(meetingService.update(id, meeting));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[MeetingController] DELETE /api/meetings/{}", id);
        meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> cancel(@PathVariable String id) {
        log.info("[MeetingController] PUT /api/meetings/{}/cancel", id);
        return ResponseEntity.ok(meetingService.cancel(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> complete(@PathVariable String id,
                                            @RequestBody Map<String, String> body) {
        log.info("[MeetingController] PUT /api/meetings/{}/complete", id);
        return ResponseEntity.ok(meetingService.complete(id, body.get("notes")));
    }

    @PutMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Meeting> addFeedback(@PathVariable String id,
                                               @RequestBody Map<String, String> body) {
        log.info("[MeetingController] PUT /api/meetings/{}/feedback", id);
        return ResponseEntity.ok(meetingService.addFeedback(id, body.get("feedback")));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Meeting>> getByTeacher(@PathVariable String teacherId) {
        log.info("[MeetingController] GET /api/meetings/teacher/{}", teacherId);
        return ResponseEntity.ok(meetingService.getByTeacher(teacherId));
    }

    @GetMapping("/teacher/{teacherId}/slots")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT')")
    public ResponseEntity<List<Map<String, Object>>> getTeacherSlots(
            @PathVariable String teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("[MeetingController] GET /api/meetings/teacher/{}/slots?date={}", teacherId, date);
        return ResponseEntity.ok(meetingService.getTeacherAvailableSlots(teacherId, date));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Meeting>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("[MeetingController] GET /api/meetings/date/{}", date);
        return ResponseEntity.ok(meetingService.getByDate(date));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Meeting>> getUpcoming() {
        log.info("[MeetingController] GET /api/meetings/upcoming");
        return ResponseEntity.ok(meetingService.getUpcoming());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("[MeetingController] GET /api/meetings/stats");
        return ResponseEntity.ok(meetingService.getStats());
    }
}
