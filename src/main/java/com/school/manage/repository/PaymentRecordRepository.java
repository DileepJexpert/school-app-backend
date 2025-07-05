package com.school.manage.repository;


import com.school.manage.model.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {

    /**
     * Finds all payment records for a specific student, ordered by payment date descending.
     * @param studentId The ID of the student.
     * @return A list of payment records.
     */
    List<PaymentRecord> findByStudentIdOrderByPaymentDateDesc(String studentId);

    /**
     * Finds the most recent payment record for a given student.
     * This was the method that caused the initial error.
     * @param studentId The ID of the student.
     * @return An Optional containing the latest payment record, if any.
     */
    Optional<PaymentRecord> findFirstByStudentIdOrderByPaymentDateDesc(String studentId);
}