package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.AppointmentNotAttendedRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.services.AppointmentStatusService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
public class AppointmentStatusController {
    private final AppointmentStatusService appointmentStatusService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public AppointmentStatusController(AppointmentStatusService appointmentStatusService) {
        this.appointmentStatusService = appointmentStatusService;
    }

    @PatchMapping("/api/citas/{id}/atendida")
    public AppointmentResponse marcarAtendida(@PathVariable UUID id) {
        return appointmentStatusService.marcarAtendida(id, VETERINARIO_DEMO_ID);
    }

    @PatchMapping("/api/citas/{id}/no-atendida")
    public AppointmentResponse marcarNoAtendida(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentNotAttendedRequest request
    ) {
        return appointmentStatusService.marcarNoAtendida(id, VETERINARIO_DEMO_ID, request);
    }
}