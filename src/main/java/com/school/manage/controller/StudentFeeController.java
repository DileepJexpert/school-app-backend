package com.school.manage.controller;

import com.school.manage.model.FeePaymentRequest;
import com.school.manage.model.PaymentRecordResponse;
import com.school.manage.model.StudentFeeProfileResponse;
import com.school.manage.model.FeeReportResponse;
import com.school.manage.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentFeeController {

    private final FeeService feeService;

    @PostMapping("/fees/collect")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<PaymentRecordResponse> collectFee(@Valid @RequestBody FeePaymentRequest request) {
        log.info("[StudentFeeController] POST /api/fees/collect — studentId='{}', amount='{}'",
                request.getStudentId(), request.getAmount());
        PaymentRecordResponse response = feeService.collectFee(request);
        log.info("[StudentFeeController] Fee collected: receiptNo='{}'", response.getReceiptNumber());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/student-fee-profiles/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT','STUDENT','PARENT')")
    public ResponseEntity<StudentFeeProfileResponse> getStudentFeeProfile(@PathVariable String studentId) {
        log.debug("[StudentFeeController] GET /api/student-fee-profiles/{}", studentId);
        return ResponseEntity.ok(feeService.getStudentFeeProfile(studentId));
    }

    @GetMapping("/fees/dues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<StudentFeeProfileResponse>> getOutstandingDues() {
        log.debug("[StudentFeeController] GET /api/fees/dues");
        return ResponseEntity.ok(feeService.getOutstandingDues());
    }

    @GetMapping("/fees/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<StudentFeeProfileResponse>> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String rollNumber) {
        log.debug("[StudentFeeController] GET /api/fees/search — name='{}', class='{}', roll='{}'",
                name, className, rollNumber);
        return ResponseEntity.ok(feeService.searchStudents(name, className, rollNumber));
    }

    @GetMapping("/fees/reports/collection-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<FeeReportResponse> getCollectionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[StudentFeeController] GET /api/fees/reports/collection-summary — from='{}', to='{}'",
                startDate, endDate);
        return ResponseEntity.ok(feeService.generateCollectionReport(startDate, endDate));
    }
}
