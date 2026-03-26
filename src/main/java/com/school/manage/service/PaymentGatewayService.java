package com.school.manage.service;

import com.school.manage.config.RazorpayConfig;
import com.school.manage.model.PaymentOrder;
import com.school.manage.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final RazorpayConfig razorpayConfig;

    /**
     * Create a payment order. In a real implementation, this would call
     * Razorpay's Order API. For now, it creates a local record.
     */
    public PaymentOrder createOrder(String studentId, String studentName, String className,
                                     String installmentId, String installmentLabel,
                                     double amount, String parentEmail, String parentPhone) {
        PaymentOrder order = new PaymentOrder();
        order.setStudentId(studentId);
        order.setStudentName(studentName);
        order.setClassName(className);
        order.setInstallmentId(installmentId);
        order.setInstallmentLabel(installmentLabel);
        order.setAmount(amount);
        order.setParentEmail(parentEmail);
        order.setParentPhone(parentPhone);
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        // In production: call Razorpay API to create order
        // RazorpayClient client = new RazorpayClient(razorpayConfig.getKeyId(), razorpayConfig.getKeySecret());
        // Order razorpayOrder = client.orders.create(orderRequest);
        // order.setRazorpayOrderId(razorpayOrder.get("id"));
        order.setRazorpayOrderId("order_" + System.currentTimeMillis());

        PaymentOrder saved = paymentOrderRepository.save(order);
        log.info("[PaymentGatewayService] Order created: id='{}', amount={}, student='{}'",
                saved.getRazorpayOrderId(), amount, studentName);
        return saved;
    }

    /**
     * Verify payment signature from Razorpay checkout callback.
     */
    public PaymentOrder verifyPayment(String razorpayOrderId, String razorpayPaymentId,
                                       String razorpaySignature) {
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + razorpayOrderId));

        // Verify signature: HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, secret)
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String expectedSignature = hmacSha256(payload, razorpayConfig.getKeySecret());

        if (expectedSignature.equals(razorpaySignature)) {
            order.setStatus("PAID");
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setRazorpaySignature(razorpaySignature);
            order.setPaidAt(LocalDateTime.now());
            log.info("[PaymentGatewayService] Payment verified: orderId='{}', paymentId='{}'",
                    razorpayOrderId, razorpayPaymentId);
        } else {
            order.setStatus("FAILED");
            order.setFailureReason("Signature verification failed");
            log.warn("[PaymentGatewayService] Payment verification FAILED: orderId='{}'", razorpayOrderId);
        }

        return paymentOrderRepository.save(order);
    }

    /**
     * Handle Razorpay webhook event.
     */
    public void handleWebhook(Map<String, Object> payload) {
        log.info("[PaymentGatewayService] Webhook received: {}", payload.get("event"));
        // Process webhook events: payment.captured, payment.failed, refund.created, etc.
    }

    public PaymentOrder getOrderStatus(String razorpayOrderId) {
        return paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + razorpayOrderId));
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}
