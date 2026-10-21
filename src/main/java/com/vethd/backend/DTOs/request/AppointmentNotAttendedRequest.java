package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AppointmentNotAttendedRequest(
        @NotBlank(message = "El motivo es obligatorio")
        String reason
) {
    
}