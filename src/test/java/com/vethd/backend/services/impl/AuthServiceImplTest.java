package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.RegisterRequest;
import com.vethd.backend.DTOs.response.AuthResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role ownerRole;

    @BeforeEach
    void setUp() {
        ownerRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_OWNER")
                .description("Rol de propietario")
                .build();
    }

    @Test
    void shouldRegisterOwnerSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .dni("12345678")
                .firstName("Carlos")
                .lastName("Gomez")
                .email("carlos@example.com")
                .password("password123")
                .phone("987654321")
                .address("Av. Siempreviva 742")
                .build();

        when(userRepository.existsByEmail("carlos@example.com")).thenReturn(false);
        when(userRepository.existsByDni("12345678")).thenReturn(false);
        when(roleRepository.findByName("ROLE_OWNER")).thenReturn(Optional.of(ownerRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPasswordHash");

        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(userId);
            return u;
        });

        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> {
            Owner o = invocation.getArgument(0);
            o.setId(ownerId);
            return o;
        });

        when(jwtService.generateToken(eq(userId), eq("carlos@example.com"), eq("ROLE_OWNER")))
                .thenReturn("mocked-jwt-token");

        AuthResponse response = authService.registerOwner(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("carlos@example.com", response.email());
        assertEquals("Carlos Gomez", response.nombre());
        assertEquals("propietario", response.rol());
        assertEquals(userId, response.id());
        assertEquals(ownerId, response.propietarioId());
        assertNotNull(response.user());
        assertEquals("987654321", response.user().telefono());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .nombre("Juan Perez")
                .email("juan@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("juan@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.registerOwner(request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("correo electrónico ya se encuentra registrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenNameIsMissing() {
        RegisterRequest request = RegisterRequest.builder()
                .email("noname@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("noname@example.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.registerOwner(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("nombre es obligatorio"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldHandleFrontendPayloadWithCombinedNombreAndSpanishFields() {
        RegisterRequest request = RegisterRequest.builder()
                .nombre("Ana Maria Gomez")
                .email("ana@example.com")
                .password("secret123")
                .telefono("999888777")
                .direccion("Calle Luna 123")
                .build();

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_OWNER")).thenReturn(Optional.of(ownerRole));
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encodedSecret");

        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(userId);
            assertEquals("Ana", u.getFirstName());
            assertEquals("Maria Gomez", u.getLastName());
            return u;
        });

        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> {
            Owner o = invocation.getArgument(0);
            o.setId(ownerId);
            assertEquals("999888777", o.getPhone());
            assertEquals("Calle Luna 123", o.getAddress());
            return o;
        });

        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token-ana");

        AuthResponse response = authService.registerOwner(request);

        assertNotNull(response);
        assertEquals("jwt-token-ana", response.token());
        assertEquals("Ana Maria Gomez", response.nombre());
        assertEquals("999888777", response.telefono());
        assertEquals("Calle Luna 123", response.direccion());
    }

    @Test
    void shouldThrowConflictWhenDniAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .dni("87654321")
                .firstName("Luis")
                .lastName("Perez")
                .email("luis@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("luis@example.com")).thenReturn(false);
        when(userRepository.existsByDni("87654321")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.registerOwner(request)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("DNI ya se encuentra registrado"));
        verify(userRepository, never()).save(any());
    }
}
