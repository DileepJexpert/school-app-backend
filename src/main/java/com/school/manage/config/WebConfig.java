package com.school.manage.config;

import com.school.manage.tenant.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    // CORS is handled by Spring Security (SecurityConfig.corsConfigurationSource).
    // WebMvcConfigurer CORS is NOT used — Spring Security's filter runs first
    // and Spring Security CORS takes precedence for all secured requests.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply tenant interceptor to all routes — it internally skips /platform/** and /actuator/**
        registry.addInterceptor(tenantInterceptor).addPathPatterns("/**");
    }
}