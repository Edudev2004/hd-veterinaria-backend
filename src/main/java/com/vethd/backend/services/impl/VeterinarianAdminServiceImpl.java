package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.VeterinarianCreateRequest;
import com.vethd.backend.DTOs.request.VeterinarianUpdateRequest;
import com.vethd.backend.DTOs.response.AdminVeterinarianResponse;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.model.User;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.SpecialtyRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.VeterinarianAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinarianAdminServiceImpl implements VeterinarianAdminService {

    private final VeterinarianRepository veterinarianRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVeterinarianResponse> listar(String nombre, String estado, Pageable pageable) {
        String filtroNombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;
        Boolean filtroEstado = null;
        if (estado != null && !estado.trim().isEmpty()) {
            if (estado.equalsIgnoreCase("ACTIVO") || estado.equalsIgnoreCase("true")) {
                filtroEstado = true;
            } else if (estado.equalsIgnoreCase("INACTIVO") || estado.equalsIgnoreCase("false")) {
                filtroEstado = false;
            }
        }

        return veterinarianRepository.listarConFiltros(filtroNombre, filtroEstado, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AdminVeterinarianResponse registrar(VeterinarianCreateRequest dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo electrónico ya se encuentra registrado");
        }
        if (userRepository.existsByDni(dto.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI ya se encuentra registrado");
        }

        Role vetRole = roleRepository.findByNameIgnoreCase("VET")
                .or(() -> roleRepository.findByNameIgnoreCase("VETERINARIAN"))
                .or(() -> roleRepository.findByNameIgnoreCase("ROLE_VET"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol veterinario no configurado"));

        User user = User.builder()
                .dni(dto.getDni().trim())
                .firstName(dto.getFirstName().trim())
                .lastName(dto.getLastName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder != null ? passwordEncoder.encode(dto.getPassword()) : dto.getPassword())
                .photoUrl(dto.getPhotoUrl())
                .active(true)
                .role(vetRole)
                .build();

        user = userRepository.save(user);

        Set<Specialty> specialties = new HashSet<>();
        if (dto.getSpecialtyIds() != null && !dto.getSpecialtyIds().isEmpty()) {
            specialties.addAll(specialtyRepository.findAllById(dto.getSpecialtyIds()));
        }

        Veterinarian vet = Veterinarian.builder()
                .user(user)
                .active(true)
                .specialties(specialties)
                .build();

        return mapToResponse(veterinarianRepository.save(vet));
    }

    @Override
    @Transactional
    public AdminVeterinarianResponse editar(UUID id, VeterinarianUpdateRequest dto) {
        Veterinarian vet = veterinarianRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

        User user = vet.getUser();

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está en uso por otro usuario");
        }

        if (!user.getDni().equalsIgnoreCase(dto.getDni()) && userRepository.existsByDni(dto.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI ya está en uso por otro usuario");
        }

        user.setDni(dto.getDni().trim());
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhotoUrl(dto.getPhotoUrl());
        userRepository.save(user);

        if (dto.getSpecialtyIds() != null) {
            Set<Specialty> specialties = new HashSet<>(specialtyRepository.findAllById(dto.getSpecialtyIds()));
            vet.setSpecialties(specialties);
        }

        return mapToResponse(veterinarianRepository.save(vet));
    }

    @Override
    @Transactional
    public AdminVeterinarianResponse cambiarEstado(UUID id) {
        Veterinarian vet = veterinarianRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

        boolean nuevoEstado = !vet.isActive();
        vet.setActive(nuevoEstado);
        vet.getUser().setActive(nuevoEstado);

        userRepository.save(vet.getUser());
        return mapToResponse(veterinarianRepository.save(vet));
    }

    private AdminVeterinarianResponse mapToResponse(Veterinarian v) {
        User u = v.getUser();
        List<String> specialtyNames = v.getSpecialties().stream()
                .map(Specialty::getName)
                .collect(Collectors.toList());

        return AdminVeterinarianResponse.builder()
                .id(v.getId())
                .userId(u.getId())
                .dni(u.getDni())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(u.getFirstName() + " " + u.getLastName())
                .email(u.getEmail())
                .photoUrl(u.getPhotoUrl())
                .active(v.isActive())
                .specialties(specialtyNames)
                .createdAt(u.getCreatedAt())
                .build();
    }
}