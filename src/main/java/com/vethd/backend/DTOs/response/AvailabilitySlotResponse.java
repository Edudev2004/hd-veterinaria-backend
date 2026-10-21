package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record AvailabilitySlotResponse(
        LocalTime hour,
        boolean available
) {
}
