
package com.vethd.backend.services.impl;

import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleServiceImpl service;

    @Test
    void debeAsignarRolAUsuario() {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("usuario@vethd.com")
                .build();

        Role role = Role.builder()
                .id(roleId)
                .name("RECEPCIONISTA")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = service.assignRole(userId, roleId);

        assertEquals(roleId, result.getRole().getId());

        verify(userRepository).save(user);
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.assignRole(userId, roleId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void debeRechazarRolInexistente() {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.assignRole(userId, roleId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void noDebeGuardarSiYaTieneElRol() {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        Role role = Role.builder()
                .id(roleId)
                .name("RECEPCIONISTA")
                .build();

        User user = User.builder()
                .id(userId)
                .role(role)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        User result = service.assignRole(userId, roleId);

        assertEquals(roleId, result.getRole().getId());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void debeRechazarIdentificadoresNulos() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.assignRole(null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verifyNoInteractions(userRepository, roleRepository);
    }
}
