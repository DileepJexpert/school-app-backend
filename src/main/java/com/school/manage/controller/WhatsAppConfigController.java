package com.school.manage.controller;

import com.school.manage.model.WhatsAppConfig;
import com.school.manage.model.WhatsAppConversation;
import com.school.manage.service.WhatsAppService;
import com.school.manage.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for configuring the WhatsApp AI agent.
 * Only SUPER_ADMIN and SCHOOL_ADMIN can manage WhatsApp settings.
 */
@Slf4j
@RestController
@RequestMapping("/api/whatsapp-config")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
public class WhatsAppConfigController {

    private final WhatsAppService whatsAppService;

    @GetMapping
    public ResponseEntity<WhatsAppConfig> getConfig() {
        String tenantId = TenantContext.getTenant();
        log.info("[WhatsAppConfig] GET config for tenant={}", tenantId);

        WhatsAppConfig config = whatsAppService.getWhatsAppConfig(tenantId);
        if (config == null) {
            // Return a default config so the UI has something to display
            config = new WhatsAppConfig();
            config.setTenantId(tenantId);
        }
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<WhatsAppConfig> updateConfig(@RequestBody WhatsAppConfig config) {
        String tenantId = TenantContext.getTenant();
        log.info("[WhatsAppConfig] PUT config for tenant={} — enabled={}", tenantId, config.isEnabled());

        config.setTenantId(tenantId);
        WhatsAppConfig saved = whatsAppService.saveConfig(config);
        // Clear parent cache since config changed
        whatsAppService.clearParentCache();
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<WhatsAppConversation>> getConversations() {
        String tenantId = TenantContext.getTenant();
        log.info("[WhatsAppConfig] GET conversations for tenant={}", tenantId);
        return ResponseEntity.ok(whatsAppService.getConversations(tenantId));
    }

    @PostMapping("/test")
    public ResponseEntity<String> testMessage(@RequestBody TestMessageRequest request) {
        String tenantId = TenantContext.getTenant();
        log.info("[WhatsAppConfig] TEST message to {} for tenant={}", request.phone(), tenantId);

        WhatsAppConfig config = whatsAppService.getWhatsAppConfig(tenantId);
        if (config == null || config.getWhatsappBusinessToken() == null) {
            return ResponseEntity.badRequest().body("WhatsApp not configured. Save your token first.");
        }

        whatsAppService.sendWhatsAppReply(request.phone(), config, request.message());
        return ResponseEntity.ok("Test message sent");
    }

    record TestMessageRequest(String phone, String message) {}
}
