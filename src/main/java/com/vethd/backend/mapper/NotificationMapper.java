package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.response.NotificationResponse;
import com.vethd.backend.model.Notification;

public class NotificationMapper {

    private NotificationMapper() {

    }

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .scheduledAt(notification.getScheduledAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
