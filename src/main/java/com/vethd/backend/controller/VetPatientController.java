package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.VetPatientResponse;
import com.vethd.backend.services.CurrentUserProvider;
import com.vethd.backend.services.VetPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VetPatientController {

    private final VetPatientService vetPatientService;
    private final CurrentUserProvider currentUserProvider;

    // Historial clínico del veterinario: lista de pacientes (mascotas) que ha atendido o tiene agendadas
    @GetMapping("/veterinarios/me/pacientes")
    public ResponseEntity<List<VetPatientResponse>> getMisPacientes() {
        return ResponseEntity.ok(vetPatientService.getPacientes(currentUserProvider.getCurrentUserId()));
    }
}