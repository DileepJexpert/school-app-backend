package com.school.manage.tenant;

/**
 * Holds the current tenant (school) identifier for the duration of an HTTP request.
 * Uses a ThreadLocal so each request thread has its own isolated tenant value.
 * CRITICAL: always call clear() after the request ends to prevent thread-pool leaks.
 */
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
