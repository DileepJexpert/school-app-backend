package com.school.manage.repository;

import com.school.manage.model.PaymentOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends MongoRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    List<PaymentOrder> findByStudentId(String studentId);

    List<PaymentOrder> findByStatus(String status);
}
