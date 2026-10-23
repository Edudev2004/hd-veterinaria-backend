
package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.SpecialtyResponse;
import com.vethd.backend.mapper.SpecialtyMapper;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.repository.SpecialtyRepository;
import com.vethd.backend.services.SpecialtyService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    // LISTAR ESPECIALIDADES ACTIVAS
    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listActive() {

        return specialtyRepository.findByActiveTrue()
                .stream()
                .map(SpecialtyMapper::toResponse)
                .toList();
    }

    // LISTAR TODAS LAS ESPECIALIDADES
    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listAll() {

        return specialtyRepository.findAll()
                .stream()
                .map(SpecialtyMapper::toResponse)
                .toList();
    }

    // CREAR ESPECIALIDAD
    @Override
    @Transactional
    public SpecialtyResponse create(
            String name,
            String description) {

        String cleanName = validateName(name);

        // Evitar nombres duplicados
        if (specialtyRepository.existsByNameIgnoreCase(cleanName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una especialidad con ese nombre"
            );
        }

        Specialty specialty = Specialty.builder()
                .name(cleanName)
                .description(description)
                .active(true)
                .build();

        Specialty saved = specialtyRepository.save(specialty);

        return SpecialtyMapper.toResponse(saved);
    }

    // EDITAR ESPECIALIDAD
    @Override
    @Transactional
    public SpecialtyResponse update(
            UUID id,
            String name,
            String description) {

        Specialty specialty = findSpecialty(id);

        String cleanName = validateName(name);

        // Evitar nombres duplicados al editar
        if (specialtyRepository.existsByNameIgnoreCaseAndIdNot(
                cleanName, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una especialidad con ese nombre"
            );
        }

        specialty.setName(cleanName);
        specialty.setDescription(description);

        Specialty updated = specialtyRepository.save(specialty);

        return SpecialtyMapper.toResponse(updated);
    }

    // ACTIVAR O DESACTIVAR ESPECIALIDAD
    @Override
    @Transactional
    public SpecialtyResponse changeStatus(
            UUID id,
            boolean active) {

        Specialty specialty = findSpecialty(id);

        // Soft delete: no elimina el registro
        specialty.setActive(active);

        Specialty updated = specialtyRepository.save(specialty);

        return SpecialtyMapper.toResponse(updated);
    }

    // BUSCAR ESPECIALIDAD POR ID
    private Specialty findSpecialty(UUID id) {

        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Especialidad no encontrada"
                ));
    }

    // VALIDAR NOMBRE DE ESPECIALIDAD
    private String validateName(String name) {

        if (name == null || name.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la especialidad es obligatorio"
            );
        }

        String cleanName = name.trim();

        if (cleanName.length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar los 100 caracteres"
            );
        }

        return cleanName;
    }
}
