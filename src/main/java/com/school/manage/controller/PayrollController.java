package com.school.manage.controller;

import com.school.manage.model.Payroll;
import com.school.manage.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Payroll>> generatePayroll(
            @RequestParam String month,
            @RequestParam int year) {
        log.info("[PayrollController] POST /api/payroll/generate — month='{}', year={}", month, year);
        return new ResponseEntity<>(payrollService.generatePayroll(month, year), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Payroll>> getByMonth(
            @RequestParam String month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getByMonth(month, year));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Payroll> getById(@PathVariable String id) {
        return ResponseEntity.ok(payrollService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Payroll> updatePayroll(
            @PathVariable String id,
            @RequestBody Payroll payroll) {
        log.info("[PayrollController] PUT /api/payroll/{}", id);
        return ResponseEntity.ok(payrollService.updatePayroll(id, payroll));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Payroll> processPayroll(@PathVariable String id) {
        log.info("[PayrollController] PUT /api/payroll/{}/process", id);
        return ResponseEntity.ok(payrollService.processPayroll(id));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Payroll> markAsPaid(@PathVariable String id) {
        log.info("[PayrollController] PUT /api/payroll/{}/pay", id);
        return ResponseEntity.ok(payrollService.markAsPaid(id));
    }

    @GetMapping("/staff/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Payroll>> getByStaff(@PathVariable String userId) {
        return ResponseEntity.ok(payrollService.getByStaff(userId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam String month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getStats(month, year));
    }
}
