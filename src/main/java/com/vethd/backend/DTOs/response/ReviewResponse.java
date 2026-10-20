package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReviewResponse(
        UUID id,
        Short rating,
        String comment,
        LocalDateTime createdAt
) {
}
