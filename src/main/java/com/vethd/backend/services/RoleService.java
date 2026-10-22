package com.vethd.backend.services;

import com.vethd.backend.model.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    // Listar todos los roles
    List<Role> listAll();

    // Buscar un rol por identificador
    Role findById(UUID id);

    // Registrar un nuevo rol
    Role create(String name, String description);

    // Actualizar un rol existente
    Role update(
            UUID id,
            String name,
            String description
    );

    // Eliminar un rol no asignado a usuarios
    void delete(UUID id);
}
