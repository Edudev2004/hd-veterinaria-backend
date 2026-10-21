package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.AppointmentNotAttendedRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.services.AppointmentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentStatusServiceImpl implements AppointmentStatusService {

    private final AppointmentRepository appointmentRepository;

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
        return AppointmentMapper.toResponse(guardado);
    }

    // US-24: marcar cita como no atendida
    @Override
    public AppointmentResponse marcarNoAtendida(UUID appointmentId, UUID veterinarianId, AppointmentNotAttendedRequest request) {
        Appointment appointment = buscarYValidar(appointmentId, veterinarianId);
        appointment.setStatus("NO_ATENDIDA");
        appointment.setCancellationReason(request.reason());
        Appointment guardado = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(guardado);
    }
}
