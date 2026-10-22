package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.RegisterRequest;
import com.vethd.backend.DTOs.response.AuthResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.AuthService;
import com.vethd.backend.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    @Override
    public AuthResponse registerOwner(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya se encuentra registrado");
        }

        String firstName = request.firstName();
        String lastName = request.lastName();

        if ((firstName == null || firstName.isBlank()) && request.nombre() != null && !request.nombre().isBlank()) {
            String[] parts = request.nombre().trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        if (firstName == null || firstName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }

        if (lastName == null) {
            lastName = "";
        }

        String dni = request.dni();
        if (dni == null || dni.isBlank()) {
            dni = "DNI" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } else {
            dni = dni.trim();
            if (userRepository.existsByDni(dni)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya se encuentra registrado");
            }
        }

        String phone = (request.phone() != null && !request.phone().isBlank())
                ? request.phone().trim()
                : (request.telefono() != null ? request.telefono().trim() : null);

        String address = (request.address() != null && !request.address().isBlank())
                ? request.address().trim()
                : (request.direccion() != null ? request.direccion().trim() : null);

        Role role = roleRepository.findByName("ROLE_OWNER")
                .or(() -> roleRepository.findByName("PROPIETARIO"))
                .or(() -> roleRepository.findByNameIgnoreCase("propietario"))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_OWNER")
                        .description("Rol de propietario de mascotas")
                        .build()));

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .dni(dni)
                .firstName(firstName)
                .lastName(lastName)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Owner owner = Owner.builder()
                .user(savedUser)
                .phone(phone)
                .address(address)
                .build();

        Owner savedOwner = ownerRepository.save(owner);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), role.getName());

        String fullName = (savedUser.getFirstName() + " " + savedUser.getLastName()).trim();

        AuthResponse.UserData userData = AuthResponse.UserData.builder()
                .id(savedUser.getId())
                .propietarioId(savedOwner.getId())
                .nombre(fullName)
                .email(savedUser.getEmail())
                .telefono(savedOwner.getPhone())
                .direccion(savedOwner.getAddress())
                .fotoUrl(savedUser.getPhotoUrl())
                .rol("propietario")
                .activo(savedUser.isActive())
                .createdAt(savedUser.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .propietarioId(savedOwner.getId())
                .nombre(fullName)
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .telefono(savedOwner.getPhone())
                .direccion(savedOwner.getAddress())
                .rol("propietario")
                .activo(savedUser.isActive())
                .createdAt(savedUser.getCreatedAt())
                .user(userData)
                .build();
    }
}
