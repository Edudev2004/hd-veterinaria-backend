package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.RoleRequest;
import com.vethd.backend.model.Role;
import com.vethd.backend.services.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleService roleService;

    // Listar todos los roles
    @GetMapping
    public ResponseEntity<List<Role>> listAll() {
        return ResponseEntity.ok(roleService.listAll());
    }

    // Obtener rol por ID
    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    // Crear rol
    @PostMapping
    public ResponseEntity<Role> create(
            @Valid @RequestBody RoleRequest request
    ) {
        Role role = roleService.create(
                request.name(),
                request.description()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(role);
    }

    // Editar rol
    @PutMapping("/{id}")
    public ResponseEntity<Role> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request
    ) {
        Role role = roleService.update(
                id,
                request.name(),
                request.description()
        );

        return ResponseEntity.ok(role);
    }

    // Eliminar rol sin usuarios asignados
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
