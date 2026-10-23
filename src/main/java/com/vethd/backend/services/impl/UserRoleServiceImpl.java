package com.vethd.backend.services.impl;

import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.UserRoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public User assignRole(UUID userId, UUID roleId) {

        if (userId == null || roleId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario y el rol son obligatorios"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El usuario no existe"
                        )
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El rol no existe"
                        )
                );

        // Evitar guardar si el usuario ya tiene el rol
        if (user.getRole() != null &&
                user.getRole().getId().equals(roleId)) {
            return user;
        }

        // Actualizar la relacion entre usuario y rol
        user.setRole(role);

        return userRepository.save(user);
    }
}
