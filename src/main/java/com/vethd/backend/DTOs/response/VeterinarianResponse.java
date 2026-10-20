package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record VeterinarianResponse(
        UUID id,
        String name,
        String specialtyName,
        String email
) {
}
