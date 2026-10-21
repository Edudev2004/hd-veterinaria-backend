package com.vethd.backend.services;

import com.vethd.backend.model.Appointment;
import com.vethd.backend.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class AppointmentStatusService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentStatusService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

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
}