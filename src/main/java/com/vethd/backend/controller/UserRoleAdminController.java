package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.UserRoleRequest;
import com.vethd.backend.DTOs.response.UserRoleResponse;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.services.UserRoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class UserRoleAdminController {

    private final UserRoleService userRoleService;

    // Asignar o cambiar rol de un usuario
    @PutMapping("/{id}/rol")
    public ResponseEntity<UserRoleResponse> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody UserRoleRequest request
    ) {

        User user = userRoleService.assignRole(
                id,
                request.roleId()
        );

        Role role = user.getRole();

        UserRoleResponse response = new UserRoleResponse(
                user.getId(),
                user.getEmail(),
                role.getId(),
                role.getName()
        );

        return ResponseEntity.ok(response);
    }
}
