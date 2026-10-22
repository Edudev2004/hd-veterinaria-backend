package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.AvailabilitySlotResponse;
import com.vethd.backend.DTOs.response.VeterinarianResponse;
import com.vethd.backend.services.VeterinarianService;
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
public class VeterinarianController {

    private final VeterinarianService veterinarianService;

    @GetMapping("/veterinarios")
    public ResponseEntity<List<VeterinarianResponse>> list(@RequestParam(name = "especialidadId", required = false) UUID especialidadId) {
        return ResponseEntity.ok(veterinarianService.listActive(especialidadId));
    }

    @GetMapping("/veterinarios/{id}/disponibilidad")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(veterinarianService.getAvailability(id, fecha));
    }
}
