package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DiagnosisResponse(
        UUID id,
        String diagnosis,
        String details,
        String treatment,
        String notes,
        LocalDateTime createdAt
) {
}
