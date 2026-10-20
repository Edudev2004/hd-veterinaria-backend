package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PetResponse(
        UUID id,
        String name,
        String species,
        String breed,
        String gender,
        LocalDate birthDate,
        String observations,
        String photoUrl,
        boolean active,
        LocalDateTime createdAt
) {
}
