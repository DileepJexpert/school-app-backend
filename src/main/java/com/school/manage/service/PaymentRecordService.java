package com.school.manage.service;


import com.school.manage.model.PaymentRecord;
import com.school.manage.repository.PaymentRecordRepository;
import com.school.manage.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final StudentRepository studentRepository; // To validate student existence

    /**
     * Creates a new payment record.
     * @param paymentRecord The payment record to be saved.
     * @return The saved payment record.
     * @throws IllegalArgumentException if the studentId is invalid.
     */
    public PaymentRecord createPayment(PaymentRecord paymentRecord) {
        // Validate that the student exists before saving a payment
        studentRepository.findById(paymentRecord.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + paymentRecord.getStudentId() + " not found."));

        // You could add logic here to generate a unique receiptNumber
        // For example:
        // paymentRecord.setReceiptNumber(generateReceiptNumber());

        return paymentRecordRepository.save(paymentRecord);
    }

    /**
     * Retrieves all payment records for a specific student.
     * @param studentId The ID of the student.
     * @return A list of payment records, or an empty list if none are found.
     */
    public List<PaymentRecord> getPaymentsForStudent(String studentId) {
        // Validate that the student exists
        if (!studentRepository.existsById(studentId)) {
            // Or throw an exception, depending on your desired API behavior
            return Collections.emptyList();
        }
        return paymentRecordRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
    }
}