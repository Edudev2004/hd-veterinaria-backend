package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.NotificationResponse;
import com.vethd.backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. GET /api/notificaciones - Retorna notificaciones del usuario en sesión
    @GetMapping("/notificaciones")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUserId);
        return ResponseEntity.ok(notifications);
    }

    // 2. PATCH /api/notificaciones/{id}/leer - Marca una notificación como leída
    @PatchMapping("/notificaciones/{id}/leer")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") UUID notificationId,
            @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        notificationService.markAsRead(notificationId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // 3. PATCH /api/notificaciones/leer-todas - Marca todas como leídas
    @PatchMapping("/notificaciones/leer-todas")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.noContent().build();
    }

    // 4. GET /api/notificaciones/no-leidas - Retorna conteo para el badge
    @GetMapping("/notificaciones/no-leidas")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(Map.of("noLeidas", count));
    }

    // 5. GET /api/recordatorios - Retorna citas del propietario en las próximas 24h
    @GetMapping("/recordatorios")
    public ResponseEntity<List<Object>> getReminders(
            @RequestHeader("X-User-Id") UUID currentUserId
    ) {
        List<Object> reminders = notificationService.getRemindersNext24Hours(currentUserId);
        return ResponseEntity.ok(reminders);
    }
}