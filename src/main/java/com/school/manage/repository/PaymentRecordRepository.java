package com.school.manage.repository;

import com.school.manage.model.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {
    List<PaymentRecord> findByStudentIdOrderByPaymentDateDesc(String studentId);
    // Custom queries can be added here if needed
}
