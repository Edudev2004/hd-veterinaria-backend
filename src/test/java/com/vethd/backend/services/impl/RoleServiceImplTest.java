package com.vethd.backend.services.impl;

import com.vethd.backend.model.Role;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void debeCrearRol() {

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Role role = roleService.create(
                "RECEPCIONISTA",
                "Personal de recepcion"
        );

        assertEquals("RECEPCIONISTA", role.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void debeRechazarNombreDuplicado() {

        when(roleRepository.existsByNameIgnoreCase("RECEPCIONISTA"))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.create(
                        "RECEPCIONISTA",
                        "Rol duplicado"
                )
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void debeRechazarNombreVacio() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.create("  ", "Sin nombre")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void debeRetornar404SiRolNoExiste() {

        UUID id = UUID.randomUUID();

        when(roleRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.findById(id)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void debeImpedirEliminarRolAsignado() {

        UUID id = UUID.randomUUID();

        Role role = Role.builder()
                .id(id)
                .name("RECEPCIONISTA")
                .build();

        when(roleRepository.findById(id))
                .thenReturn(Optional.of(role));

        when(userRepository.existsByRole_Id(id))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.delete(id)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void debeImpedirEliminarRolEsencial() {

        UUID id = UUID.randomUUID();

        Role role = Role.builder()
                .id(id)
                .name("ROLE_OWNER")
                .build();

        when(roleRepository.findById(id))
                .thenReturn(Optional.of(role));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.delete(id)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void debeImpedirCrearRolReservado() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.create(
                        "ADMIN",
                        "Administrador"
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void debeActualizarRol() {

        UUID id = UUID.randomUUID();

        Role role = Role.builder()
                .id(id)
                .name("RECEPCIONISTA")
                .description("Descripcion anterior")
                .build();

        when(roleRepository.findById(id))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Role updated = roleService.update(
                id,
                "ASISTENTE",
                "Nueva descripcion"
        );

        assertEquals("ASISTENTE", updated.getName());
        assertEquals("Nueva descripcion", updated.getDescription());
    }

    @Test
    void debeImpedirEditarRolEsencial() {

        UUID id = UUID.randomUUID();

        Role role = Role.builder()
                .id(id)
                .name("ROLE_ADMIN")
                .build();

        when(roleRepository.findById(id))
                .thenReturn(Optional.of(role));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> roleService.update(
                        id,
                        "SUPERVISOR",
                        "Otro nombre"
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        verify(roleRepository, never()).save(any(Role.class));
    }
}
