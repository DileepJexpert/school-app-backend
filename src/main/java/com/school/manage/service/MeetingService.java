package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Meeting;
import com.school.manage.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public Meeting create(Meeting meeting) {
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setStatus("SCHEDULED");
        if (meeting.getDurationMinutes() == 0) {
            meeting.setDurationMinutes(30);
        }
        // Calculate endTime if not set
        if (meeting.getEndTime() == null && meeting.getStartTime() != null) {
            meeting.setEndTime(meeting.getStartTime().plusMinutes(meeting.getDurationMinutes()));
        }

        // Check for time conflicts (same teacher, overlapping time on same date)
        if (meeting.getTeacherId() != null && meeting.getDate() != null && meeting.getStartTime() != null) {
            List<Meeting> teacherMeetings = meetingRepository
                    .findByTeacherIdAndDate(meeting.getTeacherId(), meeting.getDate());
            for (Meeting existing : teacherMeetings) {
                if ("CANCELLED".equals(existing.getStatus())) {
                    continue;
                }
                if (existing.getStartTime() != null && existing.getEndTime() != null) {
                    LocalTime newStart = meeting.getStartTime();
                    LocalTime newEnd = meeting.getEndTime();
                    if (newStart.isBefore(existing.getEndTime()) && newEnd.isAfter(existing.getStartTime())) {
                        throw new IllegalStateException(
                                "Time conflict: teacher already has a meeting from "
                                        + existing.getStartTime() + " to " + existing.getEndTime());
                    }
                }
            }
        }

        log.info("[MeetingService] Creating meeting title='{}' on date={}", meeting.getTitle(), meeting.getDate());
        return meetingRepository.save(meeting);
    }

    public Meeting update(String id, Meeting meeting) {
        Meeting existing = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        existing.setTitle(meeting.getTitle());
        existing.setDescription(meeting.getDescription());
        existing.setMeetingType(meeting.getMeetingType());
        existing.setTeacherId(meeting.getTeacherId());
        existing.setTeacherName(meeting.getTeacherName());
        existing.setParentId(meeting.getParentId());
        existing.setParentName(meeting.getParentName());
        existing.setStudentId(meeting.getStudentId());
        existing.setStudentName(meeting.getStudentName());
        existing.setClassName(meeting.getClassName());
        existing.setDate(meeting.getDate());
        existing.setStartTime(meeting.getStartTime());
        existing.setEndTime(meeting.getEndTime());
        existing.setDurationMinutes(meeting.getDurationMinutes());
        existing.setVenue(meeting.getVenue());
        existing.setStatus(meeting.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        log.info("[MeetingService] Updated meeting id='{}'", id);
        return meetingRepository.save(existing);
    }

    public void delete(String id) {
        meetingRepository.deleteById(id);
        log.info("[MeetingService] Deleted meeting id='{}'", id);
    }

    public Meeting cancel(String id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        meeting.setStatus("CANCELLED");
        meeting.setUpdatedAt(LocalDateTime.now());
        log.info("[MeetingService] Cancelled meeting id='{}'", id);
        return meetingRepository.save(meeting);
    }

    public Meeting complete(String id, String notes) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        meeting.setStatus("COMPLETED");
        meeting.setNotes(notes);
        meeting.setUpdatedAt(LocalDateTime.now());
        log.info("[MeetingService] Completed meeting id='{}'", id);
        return meetingRepository.save(meeting);
    }

    public Meeting addFeedback(String id, String feedback) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
        meeting.setFeedback(feedback);
        meeting.setUpdatedAt(LocalDateTime.now());
        log.info("[MeetingService] Added feedback to meeting id='{}'", id);
        return meetingRepository.save(meeting);
    }

    public List<Meeting> getAll() {
        return meetingRepository.findAllByOrderByDateDescStartTimeDesc();
    }

    public Meeting getById(String id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
    }

    public List<Meeting> getByTeacher(String teacherId) {
        return meetingRepository.findByTeacherId(teacherId);
    }

    public List<Meeting> getByParent(String parentId) {
        return meetingRepository.findByParentId(parentId);
    }

    public List<Meeting> getByStudent(String studentId) {
        return meetingRepository.findByStudentId(studentId);
    }

    public List<Meeting> getByDate(LocalDate date) {
        return meetingRepository.findByDate(date);
    }

    public List<Map<String, Object>> getTeacherAvailableSlots(String teacherId, LocalDate date) {
        List<Meeting> booked = meetingRepository.findByTeacherIdAndDate(teacherId, date);

        // Collect all booked time ranges (excluding cancelled)
        Set<LocalTime> bookedStartTimes = booked.stream()
                .filter(m -> !"CANCELLED".equals(m.getStatus()))
                .filter(m -> m.getStartTime() != null)
                .map(Meeting::getStartTime)
                .collect(Collectors.toSet());

        // Generate 30-min slots from 9:00 to 16:00
        List<Map<String, Object>> slots = new ArrayList<>();
        LocalTime slotStart = LocalTime.of(9, 0);
        LocalTime dayEnd = LocalTime.of(16, 0);

        while (slotStart.isBefore(dayEnd)) {
            LocalTime slotEnd = slotStart.plusMinutes(30);
            // Check if this slot conflicts with any booked meeting
            boolean isAvailable = true;
            for (Meeting m : booked) {
                if ("CANCELLED".equals(m.getStatus()) || m.getStartTime() == null || m.getEndTime() == null) {
                    continue;
                }
                if (slotStart.isBefore(m.getEndTime()) && slotEnd.isAfter(m.getStartTime())) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable) {
                Map<String, Object> slot = new LinkedHashMap<>();
                slot.put("startTime", slotStart.toString());
                slot.put("endTime", slotEnd.toString());
                slots.add(slot);
            }
            slotStart = slotEnd;
        }

        return slots;
    }

    public List<Meeting> getByDateRange(LocalDate from, LocalDate to) {
        return meetingRepository.findByDateBetween(from, to);
    }

    public List<Meeting> getUpcoming() {
        LocalDate today = LocalDate.now();
        List<Meeting> all = meetingRepository.findAllByOrderByDateDescStartTimeDesc();
        return all.stream()
                .filter(m -> m.getDate() != null && !m.getDate().isBefore(today))
                .filter(m -> "SCHEDULED".equals(m.getStatus()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<Meeting> all = meetingRepository.findAll();
        stats.put("total", (long) all.size());
        stats.put("scheduledCount", all.stream().filter(m -> "SCHEDULED".equals(m.getStatus())).count());
        stats.put("completedCount", all.stream().filter(m -> "COMPLETED".equals(m.getStatus())).count());
        stats.put("cancelledCount", all.stream().filter(m -> "CANCELLED".equals(m.getStatus())).count());
        return stats;
    }
}
