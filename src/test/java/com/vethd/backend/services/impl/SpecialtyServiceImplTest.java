
package com.vethd.backend.services.impl;

import com.vethd.backend.model.Specialty;
import com.vethd.backend.repository.SpecialtyRepository;

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
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository repository;

    @InjectMocks
    private SpecialtyServiceImpl service;

    // PRUEBA 1: CREAR ESPECIALIDAD
    @Test
    void debeCrearEspecialidad() {

        when(repository.save(any(Specialty.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(
                "Cardiologia",
                "Atencion cardiaca"
        );

        verify(repository, times(1))
                .save(any(Specialty.class));
    }

    // PRUEBA 2: RECHAZAR NOMBRE DUPLICADO
    @Test
    void debeRechazarNombreDuplicado() {

        when(repository.existsByNameIgnoreCase("Cardiologia"))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        "Cardiologia",
                        "Especialidad duplicada"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(repository, never())
                .save(any(Specialty.class));
    }

    // PRUEBA 3: EDITAR ESPECIALIDAD
    @Test
    void debeEditarEspecialidad() {

        UUID id = UUID.randomUUID();

        Specialty specialty = Specialty.builder()
                .id(id)
                .name("Medicina General")
                .active(true)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(specialty));

        when(repository.save(any(Specialty.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.update(
                id,
                "Medicina Interna",
                "Nueva descripcion"
        );

        assertEquals(
                "Medicina Interna",
                specialty.getName()
        );

        assertEquals(
                "Nueva descripcion",
                specialty.getDescription()
        );

        verify(repository).save(specialty);
    }

    // PRUEBA 4: DESACTIVAR ESPECIALIDAD
    @Test
    void debeDesactivarEspecialidad() {

        UUID id = UUID.randomUUID();

        Specialty specialty = Specialty.builder()
                .id(id)
                .name("Dermatologia")
                .active(true)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(specialty));

        when(repository.save(any(Specialty.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.changeStatus(id, false);

        assertFalse(specialty.isActive());

        verify(repository).save(specialty);
    }

    // PRUEBA 5: ACTIVAR ESPECIALIDAD
    @Test
    void debeActivarEspecialidad() {

        UUID id = UUID.randomUUID();

        Specialty specialty = Specialty.builder()
                .id(id)
                .name("Dermatologia")
                .active(false)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(specialty));

        when(repository.save(any(Specialty.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.changeStatus(id, true);

        assertTrue(specialty.isActive());
    }

    // PRUEBA 6: ESPECIALIDAD INEXISTENTE
    @Test
    void debeRetornar404SiNoExiste() {

        UUID id = UUID.randomUUID();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.changeStatus(id, false)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                ex.getStatusCode()
        );
    }

    // PRUEBA 7: RECHAZAR NOMBRE VACIO
    @Test
    void debeRechazarNombreVacio() {

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                        "   ",
                        "Sin nombre"
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(repository, never())
                .save(any(Specialty.class));
    }

    // PRUEBA 8: RECHAZAR DUPLICADO AL EDITAR
    @Test
    void debeRechazarDuplicadoAlEditar() {

        UUID id = UUID.randomUUID();

        Specialty specialty = Specialty.builder()
                .id(id)
                .name("Medicina General")
                .active(true)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(specialty));

        when(repository.existsByNameIgnoreCaseAndIdNot(
                "Cardiologia", id))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.update(
                        id,
                        "Cardiologia",
                        "Descripcion modificada"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(repository, never())
                .save(any(Specialty.class));
    }
}
