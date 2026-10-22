package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.VetScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class VetScheduleController {
    private final VetScheduleService vetScheduleService;
    private final CurrentUserProvider currentUserProvider;
    private final VeterinarianRepository veterinarianRepository;

    public VetScheduleController(VetScheduleService vetScheduleService,
                                 CurrentUserProvider currentUserProvider,
                                 VeterinarianRepository veterinarianRepository) {
        this.vetScheduleService = vetScheduleService;
        this.currentUserProvider = currentUserProvider;
        this.veterinarianRepository = veterinarianRepository;
    }

    @GetMapping("/api/veterinarios/me/agenda")
    public List<AppointmentResponse> getAgendaDiaria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return vetScheduleService.getAgendaDiaria(resolveCurrentVeterinarianId(), fecha);
    }

    private UUID resolveCurrentVeterinarianId() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return veterinarianRepository.findByUserId(currentUserId)
                .map(Veterinarian::getId)
                .orElse(currentUserId);
    }
}