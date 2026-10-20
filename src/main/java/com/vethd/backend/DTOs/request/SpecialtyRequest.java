package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SpecialtyRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        boolean active
) {
}
