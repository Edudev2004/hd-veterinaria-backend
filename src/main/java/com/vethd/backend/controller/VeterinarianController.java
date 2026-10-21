package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.VeterinarianResponse;
import com.vethd.backend.services.VeterinarianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
