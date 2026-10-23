
package com.vethd.backend.services.impl;

import com.vethd.backend.model.Permission;
import com.vethd.backend.model.Role;
import com.vethd.backend.repository.PermissionRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionsServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void debeCrearRolConPermisos() {

        Permission permission = Permission.builder()
                .code("CITAS_LEER")
                .description("Consultar citas")
                .build();

        when(permissionRepository.findByCodeIgnoreCase("CITAS_LEER"))
                .thenReturn(Optional.of(permission));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Role role = service.create(
                "RECEPCIONISTA",
                "Personal de recepcion",
                Set.of("CITAS_LEER")
        );

        assertEquals("RECEPCIONISTA", role.getName());
        assertEquals(1, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission));
    }

    @Test
    void debeRechazarPermisoInexistente() {

        when(permissionRepository.findByCodeIgnoreCase("NO_EXISTE"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        "RECEPCIONISTA",
                        "Personal de recepcion",
                        Set.of("NO_EXISTE")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void debeRechazarPermisosNulos() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        "RECEPCIONISTA",
                        "Personal de recepcion",
                        null
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
