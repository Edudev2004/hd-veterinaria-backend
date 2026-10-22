package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.AppointmentNotAttendedRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.AppointmentStatusService;
import com.vethd.backend.services.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class AppointmentStatusController {
    private final AppointmentStatusService appointmentStatusService;
    private final CurrentUserProvider currentUserProvider;
    private final VeterinarianRepository veterinarianRepository;

    public AppointmentStatusController(AppointmentStatusService appointmentStatusService,
                                       CurrentUserProvider currentUserProvider,
                                       VeterinarianRepository veterinarianRepository) {
        this.appointmentStatusService = appointmentStatusService;
        this.currentUserProvider = currentUserProvider;
        this.veterinarianRepository = veterinarianRepository;
    }

    @PatchMapping("/api/citas/{id}/atendida")
    public AppointmentResponse marcarAtendida(@PathVariable UUID id) {
        return appointmentStatusService.marcarAtendida(id, resolveCurrentVeterinarianId());
    }

    @PatchMapping("/api/citas/{id}/no-atendida")
    public AppointmentResponse marcarNoAtendida(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentNotAttendedRequest request
    ) {
        return appointmentStatusService.marcarNoAtendida(id, resolveCurrentVeterinarianId(), request);
    }

    private UUID resolveCurrentVeterinarianId() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return veterinarianRepository.findByUserId(currentUserId)
                .map(Veterinarian::getId)
                .orElse(currentUserId);
    }
}