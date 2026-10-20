package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AppointmentCancelRequest(
        @NotBlank(message = "Cancellation reason is required")
        String cancellationReason,

        String reasonCategory
) {
}
