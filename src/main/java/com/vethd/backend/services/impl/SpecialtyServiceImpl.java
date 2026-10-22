
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

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listActive() {
        return specialtyRepository.findByActiveTrue()
                .stream()
                .map(SpecialtyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> listAll() {
        return specialtyRepository.findAll()
                .stream()
                .map(SpecialtyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SpecialtyResponse create(
            String name,
            String description) {

        String cleanName = validateName(name);

        Specialty specialty = Specialty.builder()
                .name(cleanName)
                .description(description)
                .active(true)
                .build();

        return SpecialtyMapper.toResponse(
                specialtyRepository.save(specialty)
        );
    }

    @Override
    @Transactional
    public SpecialtyResponse update(
            UUID id,
            String name,
            String description) {

        Specialty specialty = findSpecialty(id);

        specialty.setName(validateName(name));
        specialty.setDescription(description);

        return SpecialtyMapper.toResponse(
                specialtyRepository.save(specialty)
        );
    }

    @Override
    @Transactional
    public SpecialtyResponse changeStatus(
            UUID id,
            boolean active) {

        Specialty specialty = findSpecialty(id);
        specialty.setActive(active);

        return SpecialtyMapper.toResponse(
                specialtyRepository.save(specialty)
        );
    }

    private Specialty findSpecialty(UUID id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Especialidad no encontrada"
                ));
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio"
            );
        }

        String cleanName = name.trim();

        if (cleanName.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar 100 caracteres"
            );
        }

        return cleanName;
    }
}
