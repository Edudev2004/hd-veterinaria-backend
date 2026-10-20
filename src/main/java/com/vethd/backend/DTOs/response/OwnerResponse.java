package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OwnerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address
) {
}
