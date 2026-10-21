package com.vethd.backend.controller;

import com.vethd.backend.services.AppointmentStatusService;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
public class AppointmentStatusController {
    private final AppointmentStatusService appointmentStatusService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public AppointmentStatusController(AppointmentStatusService appointmentStatusService) {
        this.appointmentStatusService = appointmentStatusService;
    }
}