package com.vethd.backend.services.impl;

import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    private static final String HEADER = "X-User-Id";
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Override
    public UUID getCurrentUserId() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && (authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
            String token = authHeader.substring(7).trim();
            if (!jwtService.isTokenValid(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido, expirado o revocado tras logout");
            }
            return jwtService.extractUserId(token);
        }

        String value = request.getHeader(HEADER);
        if (value != null && !value.isBlank()) {
            return UUID.fromString(value);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta token de autorización o cabecera X-User-Id");
    }
}
