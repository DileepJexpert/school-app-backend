package com.school.manage.controller;

import com.school.manage.model.DailyDiary;
import com.school.manage.model.User;
import com.school.manage.service.DailyDiaryService;
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

@Slf4j
@RestController
@RequestMapping("/api/daily-diary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyDiaryController {

    private final DailyDiaryService dailyDiaryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<DailyDiary>> getAll() {
        return ResponseEntity.ok(dailyDiaryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<DailyDiary> getById(@PathVariable String id) {
        return ResponseEntity.ok(dailyDiaryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<DailyDiary> create(@RequestBody DailyDiary diary,
                                             Authentication auth) {
        User user = (User) auth.getPrincipal();
        diary.setTeacherId(user.getId());
        diary.setTeacherName(user.getFullName());
        log.info("[DailyDiaryController] POST /api/daily-diary — teacher='{}', class='{}', subject='{}'",
                user.getFullName(), diary.getClassName(), diary.getSubject());
        return new ResponseEntity<>(dailyDiaryService.create(diary), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<DailyDiary> update(@PathVariable String id,
                                             @RequestBody DailyDiary diary) {
        return ResponseEntity.ok(dailyDiaryService.update(id, diary));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        dailyDiaryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<DailyDiary>> getByClass(@PathVariable String className) {
        return ResponseEntity.ok(dailyDiaryService.getByClass(className));
    }

    @GetMapping("/class/{className}/date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<DailyDiary>> getByClassAndDate(
            @PathVariable String className,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dailyDiaryService.getByClassAndDate(className, date));
    }

    @GetMapping("/class/{className}/subject/{subject}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<DailyDiary>> getByClassAndSubject(
            @PathVariable String className,
            @PathVariable String subject) {
        return ResponseEntity.ok(dailyDiaryService.getByClassAndSubject(className, subject));
    }

    @GetMapping("/my-entries")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<DailyDiary>> getMyEntries(Authentication auth) {
        User user = (User) auth.getPrincipal();
        log.info("[DailyDiaryController] GET /api/daily-diary/my-entries for teacher={}", user.getId());
        return ResponseEntity.ok(dailyDiaryService.getByTeacher(user.getId()));
    }

    @GetMapping("/class/{className}/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<DailyDiary>> getByClassAndDateRange(
            @PathVariable String className,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dailyDiaryService.getByClassAndDateRange(className, from, to));
    }
}
