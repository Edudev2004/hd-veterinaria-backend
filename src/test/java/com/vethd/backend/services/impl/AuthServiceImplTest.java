package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.LoginRequest;
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

    // ==========================================
    // US-01: Registro de propietario tests
    // ==========================================

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

    // ==========================================
    // US-02: Inicio de sesión y JWT tests
    // ==========================================

    @Test
    void shouldLoginSuccessfully() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .dni("12345678")
                .firstName("Pedro")
                .lastName("Navarro")
                .email("pedro@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(ownerRole)
                .active(true)
                .build();

        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(ownerId)
                .user(existingUser)
                .phone("987654321")
                .address("Calle Sol 456")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("pedro@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("pedro@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(ownerRepository.findByUserId(userId)).thenReturn(Optional.of(owner));
        when(jwtService.generateToken(userId, "pedro@example.com", "ROLE_OWNER")).thenReturn("valid-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid-jwt-token", response.token());
        assertEquals("pedro@example.com", response.email());
        assertEquals("Pedro Navarro", response.nombre());
        assertEquals("propietario", response.rol());
        assertEquals(userId, response.id());
        assertEquals(ownerId, response.propietarioId());
        assertEquals("987654321", response.telefono());
        assertTrue(response.activo());
    }

    @Test
    void shouldThrowUnauthorizedWhenUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Credenciales incorrectas"));
    }

    @Test
    void shouldThrowUnauthorizedWhenPasswordDoesNotMatch() {
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("pedro@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(ownerRole)
                .active(true)
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("pedro@example.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmail("pedro@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Credenciales incorrectas"));
    }

    @Test
    void shouldThrowForbiddenWhenAccountIsInactive() {
        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email("inactive@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(ownerRole)
                .active(false)
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("inactive@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("desactivada"));
    }

    @Test
    void shouldThrowBadRequestWhenLoginEmailOrPasswordMissing() {
        LoginRequest requestNoEmail = LoginRequest.builder()
                .password("password123")
                .build();

        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () ->
                authService.login(requestNoEmail)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        LoginRequest requestNoPassword = LoginRequest.builder()
                .email("test@example.com")
                .build();

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                authService.login(requestNoPassword)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
    }

    @Test
    void shouldLoginWithSpanishFieldsCorreoAndContrasena() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .firstName("Rosa")
                .lastName("Flores")
                .email("rosa@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(ownerRole)
                .active(true)
                .build();

        LoginRequest request = LoginRequest.builder()
                .correo("rosa@example.com")
                .contrasena("password123")
                .build();

        when(userRepository.findByEmail("rosa@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(ownerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(jwtService.generateToken(userId, "rosa@example.com", "ROLE_OWNER")).thenReturn("jwt-token-rosa");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-rosa", response.token());
        assertEquals("rosa@example.com", response.email());
    }
}
