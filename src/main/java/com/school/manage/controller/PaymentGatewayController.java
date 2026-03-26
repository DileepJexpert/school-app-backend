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
        String studentId = (String) body.get("studentId");
        String installmentId = (String) body.get("installmentId");
        double amount = Double.parseDouble(body.get("amount").toString());
        String currency = (String) body.getOrDefault("currency", "INR");
        log.info("Creating payment order for student: {}, amount: {}", studentId, amount);
        return ResponseEntity.ok(
                paymentGatewayService.createOrder(studentId, installmentId, amount, currency));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('PARENT','STUDENT')")
    public ResponseEntity<PaymentOrder> verifyPayment(@RequestBody Map<String, String> body) {
        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String razorpaySignature = body.get("razorpay_signature");
        log.info("Verifying payment for order: {}", razorpayOrderId);
        return ResponseEntity.ok(
                paymentGatewayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        log.info("Received Razorpay webhook");
        paymentGatewayService.handleWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/status/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentOrder> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentGatewayService.getOrderStatus(orderId));
    }
}
