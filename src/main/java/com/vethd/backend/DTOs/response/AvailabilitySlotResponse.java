package com.vethd.backend.DTOs.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record AvailabilitySlotResponse(
        @JsonFormat(pattern = "HH:mm")
        LocalTime hour,
        boolean available
) {
}
