package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID id,
        String title,
        String message,
        String type,
        boolean read,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt
) {
}
