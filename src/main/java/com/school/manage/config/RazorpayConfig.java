package com.school.manage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Razorpay payment gateway configuration.
 *
 * Set these in application.properties or environment variables:
 *   razorpay.key.id=rzp_test_xxxxxxxxx
 *   razorpay.key.secret=xxxxxxxxx
 *   razorpay.webhook.secret=xxxxxxxxx
 */
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String keyId;

    @Value("${razorpay.key.secret:placeholder_secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:placeholder_webhook_secret}")
    private String webhookSecret;

    public String getKeyId() { return keyId; }
    public String getKeySecret() { return keySecret; }
    public String getWebhookSecret() { return webhookSecret; }
}
