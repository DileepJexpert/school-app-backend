package com.school.manage.controller;

import com.school.manage.model.AiConfig;
import com.school.manage.model.AiUsageRecord;
import com.school.manage.model.User;
import com.school.manage.repository.AiConfigRepository;
import com.school.manage.service.AiHomeworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiConfigController {

    private final AiConfigRepository aiConfigRepository;
    private final AiHomeworkService aiHomeworkService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<AiConfig> getConfig(Authentication auth) {
        User user = (User) auth.getPrincipal();
        AiConfig config = aiConfigRepository.findByTenantId(user.getTenantId())
                .orElseGet(() -> {
                    AiConfig c = new AiConfig();
                    c.setTenantId(user.getTenantId());
                    return aiConfigRepository.save(c);
                });
        return ResponseEntity.ok(config);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<AiConfig> updateConfig(@RequestBody AiConfig update,
                                                  Authentication auth) {
        User user = (User) auth.getPrincipal();
        AiConfig existing = aiConfigRepository.findByTenantId(user.getTenantId())
                .orElseGet(() -> {
                    AiConfig c = new AiConfig();
                    c.setTenantId(user.getTenantId());
                    return c;
                });

        existing.setEnabled(update.isEnabled());
        existing.setEnabledModes(update.getEnabledModes());
        existing.setPrimaryProvider(update.getPrimaryProvider());
        existing.setFallbackProvider(update.getFallbackProvider());
        existing.setGeminiApiKey(update.getGeminiApiKey());
        existing.setClaudeApiKey(update.getClaudeApiKey());
        existing.setOllamaBaseUrl(update.getOllamaBaseUrl());
        existing.setOllamaModel(update.getOllamaModel());
        existing.setGeminiModel(update.getGeminiModel());
        existing.setClaudeModel(update.getClaudeModel());
        existing.setDailyLimitPerStudent(update.getDailyLimitPerStudent());
        existing.setMaxConversationTurns(update.getMaxConversationTurns());
        existing.setMonthlyBudgetCents(update.getMonthlyBudgetCents());
        existing.setAllowedSubjects(update.getAllowedSubjects());
        existing.setAllowedGrades(update.getAllowedGrades());
        existing.setUpdatedBy(user.getId());
        existing.setUpdatedAt(LocalDateTime.now());

        log.info("[AiConfigController] Config updated by {} — enabled={}, provider={}",
                user.getFullName(), update.isEnabled(), update.getPrimaryProvider());
        return ResponseEntity.ok(aiConfigRepository.save(existing));
    }

    @GetMapping("/usage-report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<AiUsageRecord>> getUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(aiHomeworkService.getUsageReport(from, to));
    }
}
