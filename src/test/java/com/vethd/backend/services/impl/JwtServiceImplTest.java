package com.vethd.backend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private final String secret = "aVerySecureAndLongSecretKeyForTestingJwtServiceImplementation2026!";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(secret, expirationMs);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "ROLE_OWNER";

        String token = jwtService.generateToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(role, jwtService.extractRole(token));
        assertNotNull(jwtService.extractExpiration(token));
    }

    @Test
    void shouldInvalidateTokenAndMarkAsInvalid() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "logout@example.com", "ROLE_OWNER");

        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isTokenInvalidated(token));

        jwtService.invalidateToken(token);

        assertTrue(jwtService.isTokenInvalidated(token));
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldHandleBearerPrefixWhenInvalidatingToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "bearer@example.com", "ROLE_OWNER");

        assertTrue(jwtService.isTokenValid(token));

        jwtService.invalidateToken("Bearer " + token);

        assertTrue(jwtService.isTokenInvalidated(token));
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void shouldReturnFalseForMalformedOrNullTokens() {
        assertFalse(jwtService.isTokenValid(null));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid("invalid.token.structure"));
    }
}
