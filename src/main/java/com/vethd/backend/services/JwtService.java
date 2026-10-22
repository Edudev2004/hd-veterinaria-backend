package com.vethd.backend.services;

import java.util.UUID;

public interface JwtService {
    String generateToken(UUID userId, String email, String role);
    String extractUsername(String token);
    UUID extractUserId(String token);
    String extractRole(String token);
    boolean isTokenValid(String token);
}
