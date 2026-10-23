
package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.SpecialtyRequest;
import com.vethd.backend.DTOs.request.SpecialtyStatusRequest;
import com.vethd.backend.DTOs.response.SpecialtyResponse;
import com.vethd.backend.services.SpecialtyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    // GET publico: solo especialidades activas
    @GetMapping("/especialidades")
    public ResponseEntity<List<SpecialtyResponse>> list() {
        return ResponseEntity.ok(
                specialtyService.listActive()
        );
    }

    // GET ADMIN: listar todas
    @GetMapping("/admin/especialidades")
    public ResponseEntity<List<SpecialtyResponse>> listAll() {
        return ResponseEntity.ok(
                specialtyService.listAll()
        );
    }

    // POST ADMIN: crear especialidad
    @PostMapping("/admin/especialidades")
    public ResponseEntity<SpecialtyResponse> create(
            @Valid @RequestBody SpecialtyRequest request) {

        SpecialtyResponse response = specialtyService.create(
                request.name(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // PUT ADMIN: editar especialidad
    @PutMapping("/admin/especialidades/{id}")
    public ResponseEntity<SpecialtyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialtyRequest request) {

        return ResponseEntity.ok(
                specialtyService.update(
                        id,
                        request.name(),
                        request.description()
                )
        );
    }

    // PATCH ADMIN: activar o desactivar
    @PatchMapping("/admin/especialidades/{id}/estado")
    public ResponseEntity<SpecialtyResponse> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialtyStatusRequest request) {

        return ResponseEntity.ok(
                specialtyService.changeStatus(
                        id,
                        request.active()
                )
        );
    }
}
