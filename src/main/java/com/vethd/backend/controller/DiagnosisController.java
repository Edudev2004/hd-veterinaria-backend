package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.DTOs.response.DiagnosisResponse;
import com.vethd.backend.services.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
public class DiagnosisController {
    private final DiagnosisService diagnosisService;
    private static final UUID VETERINARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/api/citas/{id}/consulta")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagnosisResponse registrarDiagnostico(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosisRequest request
    ) {
        return diagnosisService.registrarDiagnostico(id, VETERINARIO_DEMO_ID, request);
    }
}