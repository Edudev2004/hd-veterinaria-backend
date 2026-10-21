package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DiagnosisRequest(
        @NotBlank(message = "Diagnosis is required")
        String diagnosis,
        String details,
        String treatment,
        String notes
) {}