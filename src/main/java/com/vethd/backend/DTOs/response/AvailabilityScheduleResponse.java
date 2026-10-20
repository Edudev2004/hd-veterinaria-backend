package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AvailabilityScheduleResponse(
        UUID id,
        UUID veterinarianId,
        Short dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
