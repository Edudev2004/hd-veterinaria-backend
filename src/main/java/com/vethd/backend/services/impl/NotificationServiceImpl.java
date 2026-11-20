package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.NotificationResponse;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.model.Notification;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.NotificationRepository;
import com.vethd.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;

    // 1. Obtener todas las notificaciones del usuario en sesión
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 2. Marcar una notificación como leída
    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));

        // Validar que la notificación pertenezca al usuario
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar esta notificación");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // 3. Marcar todas como leídas
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // 4. Conteo de no leídas para el badge
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    // 5. Recordatorios de las próximas 24h
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getRemindersNext24Hours(UUID ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findUpcomingOwnerAppointments(ownerId, now, now.plusHours(24))
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .appointmentId(n.getAppointment() != null ? n.getAppointment().getId() : null)
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .scheduledAt(n.getScheduledAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
