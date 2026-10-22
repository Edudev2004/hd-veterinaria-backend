package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.MedicalHistoryEntryResponse;
import com.vethd.backend.DTOs.response.MedicalHistoryResponse;
import com.vethd.backend.services.PetHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetHistoryController {

    private final PetHistoryService petHistoryService;

    // US-19: Endpoint historial clínico de mascota (resumen + entradas)
    @GetMapping({"/mascotas/{id}/historial", "/mascotas/{id}/historial-clinico", "/pets/{id}/historial"})
    public ResponseEntity<MedicalHistoryResponse> getPetMedicalHistory(
            @PathVariable("id") UUID id,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) UUID veterinarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        return ResponseEntity.ok(petHistoryService.getPetMedicalHistory(id, busqueda, veterinarioId, fechaDesde, fechaHasta));
    }

    // US-19: Sub-endpoint para obtener directamente el listado de entradas clínicas
    @GetMapping({"/mascotas/{id}/historial/entradas", "/pets/{id}/historial/entradas"})
    public ResponseEntity<List<MedicalHistoryEntryResponse>> getPetMedicalHistoryEntries(
            @PathVariable("id") UUID id,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) UUID veterinarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        return ResponseEntity.ok(petHistoryService.getPetMedicalHistoryEntries(id, busqueda, veterinarioId, fechaDesde, fechaHasta));
    }
}

