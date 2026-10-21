package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentResponse(
        UUID id,
        PetResponse pet,
        VeterinarianResponse veterinarian,
        LocalDateTime scheduledAt,
        String status,
        String reason,
        String cancellationReason,
        LocalDateTime createdAt
) {
}
