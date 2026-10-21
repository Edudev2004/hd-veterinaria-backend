package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.services.VetScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class VetScheduleController {
    private final VetScheduleService vetScheduleService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public VetScheduleController(VetScheduleService vetScheduleService) {
        this.vetScheduleService = vetScheduleService;
    }

    @GetMapping("/api/veterinarios/me/agenda")
    public List<AppointmentResponse> getAgendaDiaria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return vetScheduleService.getAgendaDiaria(VETERINARIO_DEMO_ID, fecha);
    }
}