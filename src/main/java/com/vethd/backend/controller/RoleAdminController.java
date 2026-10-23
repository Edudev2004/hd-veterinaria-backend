
package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.RoleRequest;
import com.vethd.backend.DTOs.response.RoleResponse;
import com.vethd.backend.model.Role;
import com.vethd.backend.services.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleService roleService;

    // Listar roles con sus permisos
    @GetMapping
    public ResponseEntity<List<RoleResponse>> listAll() {

        List<RoleResponse> responses = roleService.listAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // Consultar un rol con sus permisos
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                toResponse(roleService.findById(id))
        );
    }

    // Crear rol con nombre, descripcion y permisos
    @PostMapping
    public ResponseEntity<RoleResponse> create(
            @Valid @RequestBody RoleRequest request
    ) {
        Role role = roleService.create(
                request.name(),
                request.description(),
                request.permissions()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(role));
    }

    // Editar rol y sus permisos
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request
    ) {
        Role role = roleService.update(
                id,
                request.name(),
                request.description(),
                request.permissions()
        );

        return ResponseEntity.ok(toResponse(role));
    }

    // Eliminar rol permitido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Convertir entidad en respuesta segura
    private RoleResponse toResponse(Role role) {

        Set<String> permissions = role.getPermissions() == null
                ? Set.of()
                : role.getPermissions()
                        .stream()
                        .map(permission -> permission.getCode())
                        .collect(Collectors.toSet());

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions
        );
    }
}
