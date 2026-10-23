package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.SpecialtyResponse;

import java.util.List;
import java.util.UUID;

public interface SpecialtyService {

    // Listar solo especialidades activas
    List<SpecialtyResponse> listActive();

    // Listar todas para administración
    List<SpecialtyResponse> listAll();

    // Crear nueva especialidad
    SpecialtyResponse create(
            String name,
            String description
    );

    // Editar especialidad existente
    SpecialtyResponse update(
            UUID id,
            String name,
            String description
    );

    // Activar o desactivar especialidad
    SpecialtyResponse changeStatus(
            UUID id,
            boolean active
    );
}
