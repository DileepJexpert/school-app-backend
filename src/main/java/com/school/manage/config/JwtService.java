package com.school.manage.config;

import com.school.manage.enums.UserRole;
import com.school.manage.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret:SchoolAppDefaultSecretKeyThatIsAtLeast32BytesLong!}")
    private String secretKey;

    /** Access token validity in seconds (default: 24 h) */
    @Value("${app.jwt.expiration:86400}")
    private long jwtExpiration;

    /** Refresh token validity in seconds (default: 7 days) */
    @Value("${app.jwt.refresh-expiration:604800}")
    private long refreshExpiration;

    // ── Token generation ─────────────────────────────────────────────────────

    public String generateToken(User user) {
        return buildToken(new HashMap<>(), user, jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");
        return buildToken(claims, user, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expirationSeconds) {
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("name", user.getFullName());
        extraClaims.put("linkedEntityId", user.getLinkedEntityId());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token validation ─────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claim extraction ─────────────────────────────────────────────────────

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UserRole extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return UserRole.valueOf(role);
    }

    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", String.class));
    }

    public String extractLinkedEntityId(String token) {
        return extractClaim(token, claims -> claims.get("linkedEntityId", String.class));
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        // Ensure the key is at least 32 bytes (256 bits) for HS256
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = Arrays.copyOf(raw, Math.max(raw.length, 32));
        return Keys.hmacShaKeyFor(key);
    }
}
