package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AppointmentRequest(
        @NotNull(message = "Pet ID is required")
        UUID petId,

        @NotNull(message = "Veterinarian ID is required")
        UUID veterinarianId,

        @NotNull(message = "Specialty ID is required")
        UUID specialtyId,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Time is required")
        LocalTime hour,

        @NotBlank(message = "Reason is required")
        String reason
) {
}
