package com.pingsso.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import java.util.Map;

/**
 * Placeholder for Bearer token provider.
 * TODO: Fix JJWT parserBuilder() API issue and re-implement JWT validation
 */
@Component
public class BearerTokenProvider {

    @Value("${bearer.token.secret:your-super-secret-key-min-32-chars-for-hs512-algorithm-change-in-production}")
    private String tokenSecret;

    @Value("${bearer.token.expiration-ms:86400000}")
    private long tokenExpirationMs;

    public boolean validateToken(String token) {
        return false;
    }

    public String getEmailFromToken(String token) {
        return null;
    }

    public Claims getClaimsFromToken(String token) {
        return null;
    }

    public String generateToken(String email, Map<String, Object> additionalClaims) {
        return "mock-token";
    }

    public long getTokenExpirationMs() {
        return tokenExpirationMs;
    }

    public long getTokenExpirationSeconds() {
        return tokenExpirationMs / 1000;
    }
}
