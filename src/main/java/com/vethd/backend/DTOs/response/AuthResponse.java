package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AuthResponse(
        String token,
        UUID id,
        UUID propietarioId,
        String nombre,
        String firstName,
        String lastName,
        String email,
        String telefono,
        String direccion,
        String rol,
        boolean activo,
        LocalDateTime createdAt,
        UserData user
) {
    @Builder
    public record UserData(
            UUID id,
            UUID propietarioId,
            String nombre,
            String email,
            String telefono,
            String direccion,
            String fotoUrl,
            String rol,
            boolean activo,
            LocalDateTime createdAt
    ) {
    }
}
