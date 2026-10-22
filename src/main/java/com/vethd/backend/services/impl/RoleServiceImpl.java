
package com.vethd.backend.services.impl;

import com.vethd.backend.model.Role;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.RoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private static final Set<String> PROTECTED_ROLES = Set.of(
            "ROLE_OWNER",
            "ROLE_VET",
            "ROLE_ADMIN",
            "PROPIETARIO",
            "VETERINARIO",
            "ADMIN"
    );

    @Override
    @Transactional(readOnly = true)
    public List<Role> listAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role findById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El rol no existe"
                ));
    }

    @Override
    @Transactional
    public Role create(String name, String description) {

        String normalizedName = validateName(name);
        validateDescription(description);

        if (roleRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un rol con ese nombre"
            );
        }

        // No permitir crear roles privilegiados por esta API
        if (isProtected(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Los roles esenciales se administran internamente"
            );
        }

        Role role = Role.builder()
                .name(normalizedName)
                .description(description)
                .build();

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role update(UUID id, String name, String description) {

        Role role = findById(id);

        String normalizedName = validateName(name);
        validateDescription(description);

        if (isProtected(role.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede modificar un rol esencial"
            );
        }

        if (isProtected(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede utilizar un nombre de rol reservado"
            );
        }

        if (roleRepository.existsByNameIgnoreCaseAndIdNot(
                normalizedName, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro rol con ese nombre"
            );
        }

        role.setName(normalizedName);
        role.setDescription(description);

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        Role role = findById(id);

        if (isProtected(role.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede eliminar un rol esencial"
            );
        }

        if (userRepository.existsByRole_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El rol tiene usuarios asignados"
            );
        }

        roleRepository.delete(role);
    }

    private String validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del rol es obligatorio"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar 50 caracteres"
            );
        }

        return normalizedName;
    }

    private void validateDescription(String description) {

        if (description != null && description.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La descripcion no puede superar 200 caracteres"
            );
        }
    }

    private boolean isProtected(String name) {

        return name != null &&
                PROTECTED_ROLES.contains(
                        name.trim().toUpperCase(java.util.Locale.ROOT)
                );
    }
}
