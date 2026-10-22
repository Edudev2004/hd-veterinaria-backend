package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record AdminVeterinarianResponse (
        UUID id,
        UUID userId,
        String dni,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String photoUrl,
        boolean active,
        List<String> specialties,
        LocalDateTime createdAt
) {
}
