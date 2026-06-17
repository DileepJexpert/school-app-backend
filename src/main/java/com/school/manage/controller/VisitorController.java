package com.school.manage.controller;

import com.school.manage.model.User;
import com.school.manage.model.Visitor;
import com.school.manage.service.VisitorService;
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
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Visitor>> getAll() {
        return ResponseEntity.ok(visitorService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Visitor> getById(@PathVariable String id) {
        return ResponseEntity.ok(visitorService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Visitor> create(@RequestBody Visitor visitor,
                                          Authentication auth) {
        User user = (User) auth.getPrincipal();
        visitor.setCheckedInBy(user.getFullName());
        log.info("[VisitorController] POST /api/visitors — visitor='{}', checkedInBy='{}'",
                visitor.getVisitorName(), user.getFullName());
        return new ResponseEntity<>(visitorService.create(visitor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Visitor> update(@PathVariable String id,
                                          @RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitorService.update(id, visitor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        visitorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Visitor> checkOut(@PathVariable String id) {
        log.info("[VisitorController] POST /api/visitors/{}/checkout", id);
        return ResponseEntity.ok(visitorService.checkOut(id));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Visitor>> getToday() {
        return ResponseEntity.ok(visitorService.getToday());
    }

    @GetMapping("/checked-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Visitor>> getCheckedIn() {
        return ResponseEntity.ok(visitorService.getCheckedIn());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Visitor>> search(@RequestParam String q) {
        return ResponseEntity.ok(visitorService.search(q));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Visitor>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(visitorService.getByDateRange(from, to));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(visitorService.getStats());
    }
}
