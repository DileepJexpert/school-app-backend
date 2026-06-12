package com.school.manage.controller;

import com.school.manage.model.PaymentOrder;
import com.school.manage.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment-gateway")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/create-order")
    @PreAuthorize("hasAnyRole('PARENT','STUDENT')")
    public ResponseEntity<PaymentOrder> createOrder(@RequestBody Map<String, Object> body) {
        String studentId = requireText(body, "studentId");
        String studentName = (String) body.getOrDefault("studentName", "");
        String className = (String) body.getOrDefault("className", "");
        String installmentId = requireText(body, "installmentId");
        String installmentLabel = (String) body.getOrDefault("installmentLabel", "");
        double amount = parseAmount(body.get("amount"));
        String parentEmail = (String) body.getOrDefault("parentEmail", "");
        String parentPhone = (String) body.getOrDefault("parentPhone", "");
        log.info("Creating payment order for student: {}, amount: {}", studentId, amount);
        return ResponseEntity.ok(
                paymentGatewayService.createOrder(studentId, studentName, className,
                        installmentId, installmentLabel, amount, parentEmail, parentPhone));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('PARENT','STUDENT')")
    public ResponseEntity<PaymentOrder> verifyPayment(@RequestBody Map<String, String> body) {
        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String razorpaySignature = body.get("razorpay_signature");
        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            throw new IllegalArgumentException("razorpay_order_id, razorpay_payment_id and razorpay_signature are required");
        }
        log.info("Verifying payment for order: {}", razorpayOrderId);
        return ResponseEntity.ok(
                paymentGatewayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature));
    }

    private static String requireText(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return s;
    }

    private static double parseAmount(Object raw) {
        if (raw == null) throw new IllegalArgumentException("amount is required");
        double amount;
        try {
            amount = Double.parseDouble(raw.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("amount must be a number");
        }
        if (amount <= 0 || amount > 10_000_000) {
            throw new IllegalArgumentException("amount must be between 1 and 1,00,00,000");
        }
        return amount;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Razorpay webhook");
        paymentGatewayService.handleWebhook(payload);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/status/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentOrder> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentGatewayService.getOrderStatus(orderId));
    }
}
