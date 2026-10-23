
package com.vethd.backend.services;

import com.vethd.backend.model.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    List<Role> listAll();

    Role findById(UUID id);

    // Contratos originales: mantener compatibilidad
    Role create(String name, String description);

    Role update(
            UUID id,
            String name,
            String description
    );

    // Nuevos contratos para gestionar permisos
    Role create(
            String name,
            String description,
            Set<String> permissions
    );

    Role update(
            UUID id,
            String name,
            String description,
            Set<String> permissions
    );

    void delete(UUID id);
}
