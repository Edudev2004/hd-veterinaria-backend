package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.AppointmentNotAttendedRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Notification;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.NotificationRepository;
import com.vethd.backend.services.AppointmentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentStatusServiceImpl implements AppointmentStatusService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;

    private Appointment buscarYValidar(UUID appointmentId, UUID veterinarianId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        if (!appointment.getVeterinarian().getId().equals(veterinarianId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el veterinario asignado a esta cita");
        }
        if (!"PENDIENTE".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede actualizar citas PENDIENTES");
        }
        return appointment;
    }

    // US-23: marcar cita como atendida
    @Override
    public AppointmentResponse marcarAtendida(UUID appointmentId, UUID veterinarianId) {
        Appointment appointment = buscarYValidar(appointmentId, veterinarianId);
        appointment.setStatus("ATENDIDA");
        Appointment guardado = appointmentRepository.save(appointment);

        notificarPropietario(guardado, "Atención completada",
                "La atención de \"" + guardado.getPet().getName() + "\" fue registrada. Ya puedes valorarla.",
                "CITA_ATENDIDA");

        return AppointmentMapper.toResponse(guardado);
    }

    // US-24: marcar cita como no atendida
    @Override
    public AppointmentResponse marcarNoAtendida(UUID appointmentId, UUID veterinarianId, AppointmentNotAttendedRequest request) {
        Appointment appointment = buscarYValidar(appointmentId, veterinarianId);
        appointment.setStatus("NO_ATENDIDA");
        appointment.setCancellationReason(request.reason());
        Appointment guardado = appointmentRepository.save(appointment);

        notificarPropietario(guardado, "Cita no atendida",
                "La cita de \"" + guardado.getPet().getName() + "\" fue marcada como no atendida.",
                "CITA_NO_ATENDIDA");

        return AppointmentMapper.toResponse(guardado);
    }

    private void notificarPropietario(Appointment appointment, String title,
                                     String message, String type) {
        User ownerUser = appointment.getPet().getOwner().getUser();

        if (ownerUser == null) {
            return;
        }

        Notification notification = Notification.builder()
                .user(ownerUser)
                .appointment(appointment)
                .title(title)
                .message(message)
                .type(type)
                .scheduledAt(appointment.getScheduledAt())
                .read(false)
                .build();

        notificationRepository.save(notification);
    }
}
