package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AppointmentRescheduledRequest(
        UUID newVeterinarianId,

        @NotNull(message = "New date is required")
        LocalDate newDate,

        @NotNull(message = "New hour is required")
        LocalTime newHour
) {
}
