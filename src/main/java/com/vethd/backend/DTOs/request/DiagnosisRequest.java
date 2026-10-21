package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import java.time.LocalDate;

@Builder
public record DiagnosisRequest(
        @NotBlank(message = "Diagnosis is required")
        String diagnosis,
        String details,
        String treatment,
        String notes,
        LocalDate nextSuggestedAppointment
) {}