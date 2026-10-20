package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SpecialtyResponse(
        UUID id,
        String name,
        String description,
        boolean active
) {
}
