package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.VeterinarianCreateRequest;
import com.vethd.backend.DTOs.request.VeterinarianUpdateRequest;
import com.vethd.backend.DTOs.response.AdminVeterinarianResponse;
import com.vethd.backend.services.VeterinarianAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/veterinarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVeterinarianController {

    private final VeterinarianAdminService veterinarianAdminService;

    @GetMapping
    public ResponseEntity<Page<AdminVeterinarianResponse>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return ResponseEntity.ok(veterinarianAdminService.listar(nombre, estado, pageable));
    }

    @PostMapping
    public ResponseEntity<AdminVeterinarianResponse> registrar(@Valid @RequestBody VeterinarianCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinarianAdminService.registrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminVeterinarianResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody VeterinarianUpdateRequest dto) {
        return ResponseEntity.ok(veterinarianAdminService.editar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<AdminVeterinarianResponse> cambiarEstado(@PathVariable UUID id) {
        return ResponseEntity.ok(veterinarianAdminService.cambiarEstado(id));
    }
}