package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReviewResponse(
        UUID id,
        UUID appointmentId,
        Short rating,
        String comment,
        LocalDateTime createdAt,
        String petName,
        String petPhotoUrl,
        String ownerName
) {
}