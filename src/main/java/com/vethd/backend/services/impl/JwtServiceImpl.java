package com.vethd.backend.services.impl;

import com.vethd.backend.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey key;
    private final long expirationTimeMs;
    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public JwtServiceImpl(
            @Value("${jwt.secret:vethd_default_secret_key_at_least_256_bits_long_for_security_2026!}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationTimeMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTimeMs = expirationTimeMs;
    }

    @Override
    public String generateToken(UUID userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("correo", email)
                .claim("email", email)
                .claim("role", role)
                .claim("rol", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTimeMs))
                .signWith(key)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return getClaims(cleanToken(token)).getSubject();
    }

    @Override
    public UUID extractUserId(String token) {
        String userIdStr = getClaims(cleanToken(token)).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    @Override
    public String extractRole(String token) {
        return getClaims(cleanToken(token)).get("role", String.class);
    }

    @Override
    public Date extractExpiration(String token) {
        return getClaims(cleanToken(token)).getExpiration();
    }

    @Override
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String cleaned = cleanToken(token);
        if (isTokenInvalidated(cleaned)) {
            return false;
        }
        try {
            Claims claims = getClaims(cleaned);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String cleaned = cleanToken(token);
        try {
            Date exp = extractExpiration(cleaned);
            blacklistedTokens.put(cleaned, exp);
        } catch (Exception e) {
            blacklistedTokens.put(cleaned, new Date(System.currentTimeMillis() + expirationTimeMs));
        }
        pruneExpiredBlacklistedTokens();
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        if (token == null || token.isBlank()) {
            return true;
        }
        return blacklistedTokens.containsKey(cleanToken(token));
    }

    private void pruneExpiredBlacklistedTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }

    private String cleanToken(String token) {
        if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(cleanToken(token))
                .getPayload();
    }
}
