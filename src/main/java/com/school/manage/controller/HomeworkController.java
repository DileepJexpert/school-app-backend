package com.school.manage.controller;

import com.school.manage.model.Homework;
import com.school.manage.model.User;
import com.school.manage.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Homework> create(@RequestBody Homework homework,
                                           Authentication auth) {
        User user = (User) auth.getPrincipal();
        homework.setTeacherId(user.getId());
        homework.setTeacherName(user.getFullName());
        log.info("[HomeworkController] POST /api/homework — teacher='{}', class='{}', subject='{}'",
                user.getFullName(), homework.getClassName(), homework.getSubject());
        return new ResponseEntity<>(homeworkService.create(homework), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<Homework>> getAll(
            @RequestParam(required = false) String className) {
        if (className != null && !className.isEmpty()) {
            return ResponseEntity.ok(homeworkService.getByClassName(className));
        }
        return ResponseEntity.ok(homeworkService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Homework> getById(@PathVariable String id) {
        return ResponseEntity.ok(homeworkService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Homework> update(@PathVariable String id,
                                           @RequestBody Homework homework) {
        return ResponseEntity.ok(homeworkService.update(id, homework));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        homeworkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
