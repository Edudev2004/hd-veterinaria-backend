package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.NotificationResponse;
import com.vethd.backend.DTOs.response.AppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getUserNotifications(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);

    List<AppointmentResponse> getRemindersNext24Hours(UUID ownerId);
}
