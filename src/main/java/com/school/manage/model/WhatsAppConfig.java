package com.school.manage.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "whatsapp_config")
public class WhatsAppConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private boolean enabled = false;

    /** Meta WhatsApp Cloud API permanent access token */
    private String whatsappBusinessToken;

    /** WhatsApp Business phone number ID (from Meta dashboard) */
    private String whatsappPhoneNumberId;

    /** Token used to verify webhook during Meta setup */
    private String webhookVerifyToken = "school-whatsapp-verify-2024";

    /** Greeting message when a parent messages for the first time */
    private String welcomeMessage = "Welcome! I'm your school's AI assistant. Ask me about your child's attendance, fees, homework, or results.";

    /** Which data features parents can query */
    private List<String> enabledFeatures = List.of("ATTENDANCE", "FEES", "HOMEWORK", "RESULTS", "GENERAL");

    /** AI provider to use for generating responses (OLLAMA, GEMINI, CLAUDE) — defaults to school's AI config */
    private String aiProvider;

    /** Max messages per parent per day */
    private int dailyLimitPerParent = 30;

    /** Language preference: auto (detect from message), en, hi */
    private String defaultLanguage = "auto";

    private String updatedBy;
    private LocalDateTime updatedAt;
}
