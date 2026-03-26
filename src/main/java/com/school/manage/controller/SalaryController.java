package com.school.manage.controller;

import com.school.manage.model.SalaryRecord;
import com.school.manage.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<SalaryRecord>> generateSalary(@RequestBody Map<String, Object> body) {
        int month = ((Number) body.get("month")).intValue();
        int year = ((Number) body.get("year")).intValue();
        String generatedBy = (String) body.getOrDefault("generatedBy", "Admin");
        log.info("[SalaryController] POST /api/salary/generate month={}, year={}", month, year);
        return ResponseEntity.ok(salaryService.generateMonthlySalary(month, year, generatedBy));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<SalaryRecord>> getSalary(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(salaryService.getSalaryByMonth(month, year));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<SalaryRecord>> getSalaryByStaff(@PathVariable String staffId) {
        return ResponseEntity.ok(salaryService.getSalaryByStaff(staffId));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<SalaryRecord> markAsPaid(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(salaryService.markAsPaid(id,
                body.getOrDefault("paymentMode", "BANK_TRANSFER"),
                body.get("transactionRef")));
    }
}
