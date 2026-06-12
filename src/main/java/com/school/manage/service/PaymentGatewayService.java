package com.school.manage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.manage.config.RazorpayConfig;
import com.school.manage.enums.PaymentMode;
import com.school.manage.model.FeePaymentRequest;
import com.school.manage.model.PaymentOrder;
import com.school.manage.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final RazorpayConfig razorpayConfig;
    private final FeeService feeService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    /**
     * Create a payment order. When real Razorpay credentials are configured this
     * calls Razorpay's Orders API; otherwise it falls back to a local mock order
     * so the rest of the flow can be developed without a Razorpay account.
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

        if (razorpayConfig.isConfigured()) {
            order.setRazorpayOrderId(createRazorpayOrder(amount, studentId, installmentLabel));
        } else {
            log.warn("[PaymentGatewayService] Razorpay not configured — creating MOCK order (no real payment possible)");
            order.setRazorpayOrderId("order_mock_" + System.currentTimeMillis());
        }

        PaymentOrder saved = paymentOrderRepository.save(order);
        log.info("[PaymentGatewayService] Order created: id='{}', amount={}, student='{}'",
                saved.getRazorpayOrderId(), amount, studentName);
        return saved;
    }

    /** Calls Razorpay Orders API with HTTP Basic auth and returns the order id. */
    private String createRazorpayOrder(double amountRupees, String studentId, String label) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(razorpayConfig.getKeyId(), razorpayConfig.getKeySecret());

        Map<String, Object> body = Map.of(
                "amount", Math.round(amountRupees * 100),   // Razorpay expects paise
                "currency", "INR",
                "receipt", "rcpt_" + System.currentTimeMillis(),
                "notes", Map.of("studentId", studentId == null ? "" : studentId,
                                "installment", label == null ? "" : label)
        );

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    RAZORPAY_ORDERS_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            Object id = resp.getBody() != null ? resp.getBody().get("id") : null;
            if (id == null) throw new IllegalStateException("Razorpay returned no order id");
            return id.toString();
        } catch (Exception e) {
            log.error("[PaymentGatewayService] Razorpay order creation failed: {}", e.getMessage());
            throw new IllegalStateException("Could not create payment order. Please try again.");
        }
    }

    /**
     * Verify payment signature from the Razorpay checkout callback. On success the
     * matching fee installment is marked paid via FeeService.
     */
    public PaymentOrder verifyPayment(String razorpayOrderId, String razorpayPaymentId,
                                       String razorpaySignature) {
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new com.school.manage.exception.ResourceNotFoundException(
                        "Order not found: " + razorpayOrderId));

        // Verify signature: HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, secret)
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String expectedSignature = hmacSha256(payload, razorpayConfig.getKeySecret());

        if (expectedSignature.equals(razorpaySignature)) {
            order.setStatus("PAID");
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setRazorpaySignature(razorpaySignature);
            order.setPaidAt(LocalDateTime.now());
            paymentOrderRepository.save(order);
            log.info("[PaymentGatewayService] Payment verified: orderId='{}', paymentId='{}'",
                    razorpayOrderId, razorpayPaymentId);
            markInstallmentPaid(order);
        } else {
            order.setStatus("FAILED");
            order.setFailureReason("Signature verification failed");
            log.warn("[PaymentGatewayService] Payment verification FAILED: orderId='{}'", razorpayOrderId);
            paymentOrderRepository.save(order);
        }

        return order;
    }

    /**
     * Reuses the standard fee-collection path so an online payment shows up in
     * the fee profile, reports and receipts exactly like a counter payment.
     * Failures here are logged but never fail the request — the money is already
     * captured, so the parent must still get a success response.
     */
    private void markInstallmentPaid(PaymentOrder order) {
        try {
            if (order.getInstallmentLabel() == null || order.getInstallmentLabel().isBlank()) {
                log.warn("[PaymentGatewayService] Order {} has no installment label — skipping fee update",
                        order.getRazorpayOrderId());
                return;
            }
            FeePaymentRequest req = new FeePaymentRequest();
            req.setStudentId(order.getStudentId());
            req.setAmount(BigDecimal.valueOf(order.getAmount()));
            req.setDiscount(BigDecimal.ZERO);
            req.setInstallmentNames(List.of(order.getInstallmentLabel()));
            req.setPaymentMode(PaymentMode.DIGITAL_PAYMENT);
            req.setTransactionId(order.getRazorpayPaymentId());
            req.setRemarks("Online payment via Razorpay (order " + order.getRazorpayOrderId() + ")");
            feeService.collectFee(req);
            log.info("[PaymentGatewayService] Installment '{}' marked PAID for student {}",
                    order.getInstallmentLabel(), order.getStudentId());
        } catch (Exception e) {
            log.error("[PaymentGatewayService] Payment captured but fee profile update FAILED for order {}: {}",
                    order.getRazorpayOrderId(), e.getMessage(), e);
        }
    }

    /**
     * Handle a Razorpay webhook. Verifies the X-Razorpay-Signature against the raw
     * body using the webhook secret, then processes payment.captured / payment.failed.
     * Acts as a safety net for the synchronous verify flow (idempotent).
     */
    public void handleWebhook(String rawBody, String signature) {
        if (razorpayConfig.getWebhookSecret() != null
                && !razorpayConfig.getWebhookSecret().equals("placeholder_webhook_secret")) {
            String expected = hmacSha256(rawBody, razorpayConfig.getWebhookSecret());
            if (signature == null || !expected.equals(signature)) {
                log.warn("[PaymentGatewayService] Webhook REJECTED — invalid signature");
                return;
            }
        } else {
            log.warn("[PaymentGatewayService] Webhook secret not set — skipping signature verification");
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(rawBody, Map.class);
            String event = (String) payload.get("event");
            log.info("[PaymentGatewayService] Webhook event: {}", event);

            String orderId = extractOrderId(payload);
            if (orderId == null) return;

            paymentOrderRepository.findByRazorpayOrderId(orderId).ifPresent(order -> {
                if ("payment.captured".equals(event) && !"PAID".equals(order.getStatus())) {
                    order.setStatus("PAID");
                    order.setPaidAt(LocalDateTime.now());
                    paymentOrderRepository.save(order);
                    markInstallmentPaid(order);
                } else if ("payment.failed".equals(event)) {
                    order.setStatus("FAILED");
                    order.setFailureReason("Reported failed by Razorpay webhook");
                    paymentOrderRepository.save(order);
                }
            });
        } catch (Exception e) {
            log.error("[PaymentGatewayService] Webhook processing error: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractOrderId(Map<String, Object> payload) {
        try {
            Map<String, Object> p = (Map<String, Object>) payload.get("payload");
            Map<String, Object> payment = (Map<String, Object>) p.get("payment");
            Map<String, Object> entity = (Map<String, Object>) payment.get("entity");
            return (String) entity.get("order_id");
        } catch (Exception e) {
            log.warn("[PaymentGatewayService] Could not extract order_id from webhook payload");
            return null;
        }
    }

    public PaymentOrder getOrderStatus(String razorpayOrderId) {
        return paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new com.school.manage.exception.ResourceNotFoundException(
                        "Order not found: " + razorpayOrderId));
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
