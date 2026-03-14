package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the Bearer JWT on every request and populates the SecurityContext.
 *
 * Also sets TenantContext from the token's tenantId claim so that the correct
 * MongoDB database is selected — the JWT-based tenant takes precedence over
 * the X-Tenant-ID header to prevent tenant-hopping attacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestUri = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JwtAuthFilter] No Bearer token on request: {} {}", request.getMethod(), requestUri);
            chain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            if (jwtService.isTokenValid(jwt)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String userId   = jwtService.extractUserId(jwt);
                UserRole role   = jwtService.extractRole(jwt);
                String tenantId = jwtService.extractTenantId(jwt);

                log.debug("[JwtAuthFilter] Valid JWT — userId='{}', role='{}', tenant='{}', uri='{}'",
                        userId, role, tenantId, requestUri);

                // Pin the request to the tenant from the JWT (trusted source)
                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContext.setTenant(tenantId);
                    log.debug("[JwtAuthFilter] TenantContext set to '{}' from JWT", tenantId);
                }

                var authority = new SimpleGrantedAuthority("ROLE_" + role.name());
                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(authority));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Invalid token → request proceeds as unauthenticated;
            // Spring Security will reject it if the endpoint requires auth.
            log.warn("[JwtAuthFilter] Invalid JWT on '{}': {}", requestUri, e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
