package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AppointmentRequest(
        @NotNull(message = "Pet ID is required")
        UUID petId,

        @NotNull(message = "Veterinarian ID is required")
        UUID veterinarianId,

        @NotNull(message = "Specialty ID is required")
        UUID specialtyId,

        @NotNull(message = "Scheduled date and time are required")
        @Future(message = "Scheduled date must be in the future")
        LocalDateTime scheduledAt,

        @NotBlank(message = "Reason is required")
        String reason
) {
}
