package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "payment_orders")
public class PaymentOrder {

    @Id
    private String id;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String studentId;
    private String studentName;
    private String className;

    /** Fee installment this payment is for */
    private String installmentId;
    private String installmentLabel;

    private double amount;
    private String currency = "INR";

    // CREATED, PAID, FAILED, REFUNDED
    private String status = "CREATED";

    private String parentEmail;
    private String parentPhone;

    private String failureReason;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
}
