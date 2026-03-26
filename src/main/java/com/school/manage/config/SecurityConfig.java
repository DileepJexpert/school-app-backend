package com.school.manage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 *
 * Strategy:
 *   - Stateless JWT (no sessions)
 *   - Public: /api/auth/**, /platform/auth/**, /platform/schools/** (for tenant validation)
 *   - Public read: results, timetable, notifications (parents/students can view without login)
 *   - Public: /api/payment-gateway/webhook (Razorpay async webhook)
 *   - Public: /ws/** (WebSocket handshake)
 *   - Everything else: authenticated
 *   - Fine-grained role enforcement via @PreAuthorize on controllers
 *
 * Roles: SUPER_ADMIN, SCHOOL_ADMIN, TEACHER, ACCOUNTANT, TRANSPORT_MANAGER, STUDENT, PARENT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // -- Auth endpoints (login, refresh) -- always public
                .requestMatchers("/api/auth/**", "/platform/auth/**").permitAll()

                // -- Platform: tenant validation used by Flutter login screen
                .requestMatchers("/platform/schools/*/validate").permitAll()

                // -- Public read-only resources
                .requestMatchers(HttpMethod.GET, "/api/results/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/timetable/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notifications/**").permitAll()

                // -- Payment gateway webhook (unauthenticated Razorpay callbacks)
                .requestMatchers("/api/payment-gateway/webhook").permitAll()

                // -- WebSocket handshake endpoint
                .requestMatchers("/ws/**").permitAll()

                // -- Health / actuator
                .requestMatchers("/actuator/**").permitAll()

                // -- Everything else requires a valid JWT
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS -- permits any origin with credentials so Flutter web + mobile both work.
     * Tighten allowedOriginPatterns in production to your actual domain(s).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
