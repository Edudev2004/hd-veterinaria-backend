package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getUserNotifications(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);

    List<Object> getRemindersNext24Hours(UUID ownerId);
}
