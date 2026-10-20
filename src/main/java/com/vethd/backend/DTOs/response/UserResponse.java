package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        String dni,
        String firstName,
        String lastName,
        String email,
        String roleName,
        boolean active,
        LocalDateTime createdAt
) {
}
