package com.school.manage.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts every API request and resolves the current tenant (school).
 *
 * Tenant resolution order:
 *   1. X-Tenant-ID request header  (used by Flutter app and API clients)
 *   2. Subdomain                    (e.g., springfield.yourapp.com → "springfield")
 *
 * Platform routes (/platform/**) are excluded — they operate across all tenants.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();

        // Skip tenant resolution for platform-level routes (school registry, health checks)
        if (path.startsWith("/platform/") || path.startsWith("/actuator/")) {
            return true;
        }

        String tenantId = resolveTenant(request);

        if (tenantId == null || tenantId.isBlank()) {
            // For backwards compatibility with single-tenant deployments,
            // fall back to "default" tenant instead of rejecting the request.
            tenantId = "default";
        }

        // Sanitize: only alphanumeric + underscore to prevent DB name injection
        tenantId = tenantId.replaceAll("[^a-zA-Z0-9_\\-]", "").toLowerCase();

        TenantContext.setTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // CRITICAL: always clean up to prevent leaking tenant across thread-pool reuse
        TenantContext.clear();
    }

    private String resolveTenant(HttpServletRequest request) {
        // 1. Check header first (Flutter app sends this)
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant;
        }

        // 2. Fall back to subdomain (e.g., springfield.yourapp.com)
        String host = request.getServerName(); // e.g., springfield.yourapp.com
        if (host != null && host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            // Ignore common non-tenant subdomains
            if (!subdomain.equals("www") && !subdomain.equals("api") && !subdomain.equals("localhost")) {
                return subdomain;
            }
        }

        return null;
    }
}
