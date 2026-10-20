package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AvailabilityScheduleRequest(
        @NotNull(message = "Veterinarian ID is required")
        UUID veterinarianId,
        @NotNull(message = "Day of week is required")
        Short dayOfWeek,
        @NotNull(message = "Start time is required")
        LocalTime startTime,
        @NotNull(message = "End time is required")
        LocalTime endTime
) {
}
