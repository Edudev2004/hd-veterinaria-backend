package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.DTOs.response.DiagnosisResponse;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class DiagnosisController {
    private final DiagnosisService diagnosisService;
    private final CurrentUserProvider currentUserProvider;
    private final com.vethd.backend.repository.VeterinarianRepository veterinarianRepository;

    public DiagnosisController(DiagnosisService diagnosisService,
                               CurrentUserProvider currentUserProvider,
                               com.vethd.backend.repository.VeterinarianRepository veterinarianRepository) {
        this.diagnosisService = diagnosisService;
        this.currentUserProvider = currentUserProvider;
        this.veterinarianRepository = veterinarianRepository;
    }

    @PostMapping("/api/citas/{id}/consulta")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosisResponse registrarDiagnostico(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosisRequest request
    ) {
        return diagnosisService.registrarDiagnostico(id, resolveCurrentVeterinarianId(), request);
    }

    private UUID resolveCurrentVeterinarianId() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        return veterinarianRepository.findByUserId(currentUserId)
                .map(Veterinarian::getId)
                .orElse(currentUserId);
    }
}