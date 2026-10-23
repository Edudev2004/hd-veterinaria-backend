package com.vethd.backend.services;

import com.vethd.backend.model.User;

import java.util.UUID;

public interface UserRoleService {

    // Asignar un rol existente a un usuario
    User assignRole(UUID userId, UUID roleId);
}
