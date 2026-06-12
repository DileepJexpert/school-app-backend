package com.school.manage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.manage.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Cloud API webhook endpoint.
 *
 * Meta sends two types of requests:
 * 1. GET  — verification (challenge response) during webhook setup
 * 2. POST — incoming messages from WhatsApp users
 *
 * This endpoint is PUBLIC (no JWT required) — secured by X-Hub-Signature-256
 * verification when whatsapp.app-secret is configured.
 */
@Slf4j
@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppService whatsAppService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${whatsapp.verify-token:school-whatsapp-verify-2024}")
    private String verifyToken;

    /** Meta app secret for webhook signature verification. Empty = verification skipped (dev only). */
    @Value("${whatsapp.app-secret:}")
    private String appSecret;

    /**
     * Webhook verification — Meta sends this during setup.
     * Must return the challenge string if the verify token matches.
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.info("[WhatsApp] Webhook verification — mode={}", mode);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("[WhatsApp] Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }

        log.warn("[WhatsApp] Webhook verification FAILED — token mismatch");
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Incoming WhatsApp messages.
     * Body is consumed raw so the X-Hub-Signature-256 HMAC can be verified
     * against the exact bytes Meta signed.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        log.info("[WhatsApp] Incoming webhook payload received");

        // Signature verification (mandatory when app secret is configured)
        if (appSecret != null && !appSecret.isBlank()) {
            if (signature == null || !whatsAppService.verifySignature(signature, rawPayload, appSecret)) {
                log.warn("[WhatsApp] Webhook REJECTED — invalid or missing X-Hub-Signature-256");
                return ResponseEntity.status(403).body("Invalid signature");
            }
        } else {
            log.warn("[WhatsApp] whatsapp.app-secret not set — skipping signature verification (NOT safe for production)");
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(rawPayload, Map.class);

            // Parse the Meta Cloud API webhook format
            // Structure: { entry: [{ changes: [{ value: { messages: [...] } }] }] }
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
                if (changes == null) continue;

                for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    if (value == null) continue;

                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                    if (messages == null) continue;

                    for (Map<String, Object> message : messages) {
                        String type = (String) message.get("type");
                        String from = (String) message.get("from");
                        String messageId = (String) message.get("id");

                        if (!"text".equals(type)) {
                            log.info("[WhatsApp] Skipping non-text message type: {}", type);
                            continue;
                        }

                        Map<String, Object> textObj = (Map<String, Object>) message.get("text");
                        String body = textObj != null ? (String) textObj.get("body") : null;

                        if (from != null && body != null && !body.isBlank()) {
                            log.info("[WhatsApp] Processing message from {} — '{}'",
                                    from, body.substring(0, Math.min(50, body.length())));
                            whatsAppService.handleIncomingMessage(from, body, messageId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[WhatsApp] Error processing webhook: {}", e.getMessage(), e);
        }

        // Always return 200 to acknowledge receipt — Meta will retry otherwise
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
