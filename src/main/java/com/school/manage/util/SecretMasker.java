package com.school.manage.util;

/**
 * Masks secrets (API keys, tokens) before they leave the API,
 * and detects masked values coming back so the stored secret is preserved.
 */
public final class SecretMasker {

    private static final String MASK_PREFIX = "****";

    private SecretMasker() {}

    /** "sk-abc123xyz" → "****3xyz". Null/blank stays as-is. */
    public static String mask(String secret) {
        if (secret == null || secret.isBlank()) return secret;
        String last4 = secret.length() > 4 ? secret.substring(secret.length() - 4) : "";
        return MASK_PREFIX + last4;
    }

    /**
     * Resolve the value to store on update:
     * a masked or blank incoming value means "unchanged" — keep the existing secret.
     */
    public static String resolve(String incoming, String existing) {
        if (incoming == null || incoming.isBlank() || incoming.startsWith(MASK_PREFIX)) {
            return existing;
        }
        return incoming;
    }
}
