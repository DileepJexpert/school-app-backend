package com.school.manage.controller;


import com.school.manage.model.PaymentRecord;
import com.school.manage.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<PaymentRecord> createPayment(@RequestBody PaymentRecord paymentRecord) {
        try {
            PaymentRecord createdPayment = paymentRecordService.createPayment(paymentRecord);
            return ResponseEntity.ok(createdPayment);
        } catch (IllegalArgumentException e) {
            // Returns a 404 Not Found if the student ID is invalid
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT','STUDENT','PARENT')")
    public ResponseEntity<List<PaymentRecord>> getPaymentsByStudentId(@PathVariable String studentId) {
        List<PaymentRecord> payments = paymentRecordService.getPaymentsForStudent(studentId);
        return ResponseEntity.ok(payments);
    }
}