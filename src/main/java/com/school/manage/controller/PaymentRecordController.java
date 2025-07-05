package com.school.manage.controller;


import com.school.manage.model.PaymentRecord;
import com.school.manage.service.PaymentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRecordController {

    private final PaymentRecordService paymentRecordService;

    /**
     * API endpoint to create a new payment record.
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentRecord> createPayment(@RequestBody PaymentRecord paymentRecord) {
        try {
            PaymentRecord createdPayment = paymentRecordService.createPayment(paymentRecord);
            return ResponseEntity.ok(createdPayment);
        } catch (IllegalArgumentException e) {
            // Returns a 404 Not Found if the student ID is invalid
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * API endpoint to get all payments for a specific student.
     * GET /api/payments/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentRecord>> getPaymentsByStudentId(@PathVariable String studentId) {
        List<PaymentRecord> payments = paymentRecordService.getPaymentsForStudent(studentId);
        return ResponseEntity.ok(payments);
    }
}