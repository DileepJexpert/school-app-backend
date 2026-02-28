package com.school.manage.controller;

import com.school.manage.model.FeePaymentRequest;
import com.school.manage.model.PaymentRecordResponse;
import com.school.manage.model.StudentFeeProfileResponse;
import com.school.manage.model.FeeReportResponse; // Import the new response DTO
import com.school.manage.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api") // A common base path
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentFeeController {

    private final FeeService feeService;

    @PostMapping("/fees/collect")
    public ResponseEntity<PaymentRecordResponse> collectFee(@Valid @RequestBody FeePaymentRequest request) {
        PaymentRecordResponse paymentRecordResponse = feeService.collectFee(request);
        return new ResponseEntity<>(paymentRecordResponse, HttpStatus.CREATED);
    }

    @GetMapping("/student-fee-profiles/{studentId}")
    public ResponseEntity<StudentFeeProfileResponse> getStudentFeeProfile(@PathVariable String studentId) {
        StudentFeeProfileResponse response = feeService.getStudentFeeProfile(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for students by various criteria.
     *
     * @param name The name of the student to search for.
     * @param className The class to filter by.
     * @param rollNumber The roll number to search for.
     * @return A list of students matching the criteria.
     */
    /**
     * Returns all students with outstanding dues (dueFees > 0), sorted highest first.
     */
    @GetMapping("/fees/dues")
    public ResponseEntity<List<StudentFeeProfileResponse>> getOutstandingDues() {
        return ResponseEntity.ok(feeService.getOutstandingDues());
    }

    @GetMapping("/fees/search")
    public ResponseEntity<List<StudentFeeProfileResponse>> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String rollNumber) {

        List<StudentFeeProfileResponse> responses = feeService.searchStudents(name, className, rollNumber);
        return ResponseEntity.ok(responses);
    }

    /**
     * --- NEW ENDPOINT FOR FEE COLLECTION REPORT ---
     * Generates and returns a summary of fee collections.
     *
     * @param startDate Optional start date for the report (format: YYYY-MM-DD).
     * @param endDate   Optional end date for the report (format: YYYY-MM-DD).
     * @return A FeeReportResponse object with the aggregated data.
     */
    @GetMapping("/fees/reports/collection-summary")
    public ResponseEntity<FeeReportResponse> getCollectionReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        FeeReportResponse report = feeService.generateCollectionReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
