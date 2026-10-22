package com.vethd.backend.controller;

import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.RoleRepository;
import com.vethd.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    private static final String[] TABLAS = {
            "veterinarian_specialties", "reviews", "appointments", "pets",
            "owners", "veterinarians", "specialties", "users", "roles"
    };

    @BeforeEach
    void cleanDatabase() {
        for (String tabla : TABLAS) {
            em.createNativeQuery("DELETE FROM " + tabla).executeUpdate();
        }
    }

    // ==========================================
    // US-01: Registro de propietario
    // ==========================================

    @Test
    void shouldRegisterOwnerSuccessfullyAndReturn201WithJwt() throws Exception {
        String json = """
                {
                  "nombre": "Carlos Navarro",
                  "email": "carlos.navarro@example.com",
                  "password": "Password123!",
                  "telefono": "987654321",
                  "direccion": "Av. Las Palmeras 123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("carlos.navarro@example.com"))
                .andExpect(jsonPath("$.nombre").value("Carlos Navarro"))
                .andExpect(jsonPath("$.rol").value("propietario"))
                .andExpect(jsonPath("$.telefono").value("987654321"))
                .andExpect(jsonPath("$.direccion").value("Av. Las Palmeras 123"))
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.propietarioId").exists());

        Optional<User> savedUserOpt = userRepository.findByEmail("carlos.navarro@example.com");
        assertTrue(savedUserOpt.isPresent());
        User savedUser = savedUserOpt.get();
        assertTrue(passwordEncoder.matches("Password123!", savedUser.getPasswordHash()));

        Optional<Owner> savedOwnerOpt = ownerRepository.findByUserId(savedUser.getId());
        assertTrue(savedOwnerOpt.isPresent());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        String jsonFirst = """
                {
                  "nombre": "Maria Lopez",
                  "email": "maria@example.com",
                  "password": "Password123!",
                  "telefono": "999888777"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFirst))
                .andExpect(status().isCreated());

        String jsonDuplicate = """
                {
                  "nombre": "Maria Duplicate",
                  "email": "maria@example.com",
                  "password": "OtherPassword456!",
                  "telefono": "111222333"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDuplicate))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenEmailIsMissingOrInvalid() throws Exception {
        String jsonInvalidEmail = """
                {
                  "nombre": "Juan Sin Email",
                  "email": "invalid-email-format",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPasswordIsShorterThan8Characters() throws Exception {
        String jsonShortPassword = """
                {
                  "nombre": "Pedro Corto",
                  "email": "pedro@example.com",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonShortPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegisterOwnerWithEnglishFieldsDniFirstAndLastName() throws Exception {
        String jsonEnglish = """
                {
                  "dni": "77665544",
                  "firstName": "Lucia",
                  "lastName": "Fernandez",
                  "email": "lucia@example.com",
                  "password": "StrongPassword789!",
                  "phone": "944332211",
                  "address": "Jr. Puno 456"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEnglish))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.firstName").value("Lucia"))
                .andExpect(jsonPath("$.lastName").value("Fernandez"))
                .andExpect(jsonPath("$.email").value("lucia@example.com"))
                .andExpect(jsonPath("$.telefono").value("944332211"));

        Optional<User> userOpt = userRepository.findByDni("77665544");
        assertTrue(userOpt.isPresent());
    }

    // ==========================================
    // US-02: Inicio de sesión (Login & JWT)
    // ==========================================

    @Test
    void shouldLoginSuccessfullyAndReturn200WithJwt() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").description("Dueño").build());
        User user = userRepository.save(User.builder()
                .dni("11223344")
                .firstName("Eduardo")
                .lastName("Navarro")
                .email("eduardo@example.com")
                .passwordHash(passwordEncoder.encode("SecretPass123!"))
                .role(role)
                .active(true)
                .build());

        ownerRepository.save(Owner.builder()
                .user(user)
                .phone("988776655")
                .address("Calle Principal 100")
                .build());

        String loginJson = """
                {
                  "email": "eduardo@example.com",
                  "password": "SecretPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("eduardo@example.com"))
                .andExpect(jsonPath("$.nombre").value("Eduardo Navarro"))
                .andExpect(jsonPath("$.rol").value("propietario"))
                .andExpect(jsonPath("$.telefono").value("988776655"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.user.propietarioId").exists());
    }

    @Test
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").build());
        userRepository.save(User.builder()
                .dni("22334455")
                .firstName("Ana")
                .lastName("Torres")
                .email("ana@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPassword1!"))
                .role(role)
                .active(true)
                .build());

        String wrongPassJson = """
                {
                  "email": "ana@example.com",
                  "password": "WrongPassword999!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPassJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        String nonExistentJson = """
                {
                  "email": "nonexistent@example.com",
                  "password": "SomePassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nonExistentJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAccountIsInactive() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").build());
        userRepository.save(User.builder()
                .dni("33445566")
                .firstName("Inactivo")
                .lastName("Usuario")
                .email("inactivo@example.com")
                .passwordHash(passwordEncoder.encode("Pass123456!"))
                .role(role)
                .active(false)
                .build());

        String inactiveJson = """
                {
                  "email": "inactivo@example.com",
                  "password": "Pass123456!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLoginWithSpanishFieldsCorreoAndContrasena() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").build());
        userRepository.save(User.builder()
                .dni("44556677")
                .firstName("Jorge")
                .lastName("Rios")
                .email("jorge@example.com")
                .passwordHash(passwordEncoder.encode("JorgePass1!"))
                .role(role)
                .active(true)
                .build());

        String spanishJson = """
                {
                  "correo": "jorge@example.com",
                  "contrasena": "JorgePass1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spanishJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("jorge@example.com"));
    }

    // ==========================================
    // US-03: Cierre de sesión (Logout & Invalidación)
    // ==========================================

    @Test
    void shouldLogoutSuccessfullyWithBearerTokenAndInvalidateIt() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").build());
        userRepository.save(User.builder()
                .dni("55667788")
                .firstName("Marcos")
                .lastName("Silva")
                .email("marcos@example.com")
                .passwordHash(passwordEncoder.encode("Marcos123!"))
                .role(role)
                .active(true)
                .build());

        String loginJson = """
                {
                  "email": "marcos@example.com",
                  "password": "Marcos123!"
                }
                """;

        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response JSON
        String token = responseContent.split("\"token\":\"")[1].split("\"")[0];

        // 1. First logout call -> 200 OK
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada correctamente"));

        // 2. Calling logout again with the same token -> 401 Unauthorized (token is blacklisted)
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutSuccessfullyWithTokenInBody() throws Exception {
        Role role = roleRepository.save(Role.builder().name("ROLE_OWNER").build());
        userRepository.save(User.builder()
                .dni("66778899")
                .firstName("Claudia")
                .lastName("Mendoza")
                .email("claudia@example.com")
                .passwordHash(passwordEncoder.encode("Claudia123!"))
                .role(role)
                .active(true)
                .build());

        String loginJson = """
                {
                  "email": "claudia@example.com",
                  "password": "Claudia123!"
                }
                """;

        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = responseContent.split("\"token\":\"")[1].split("\"")[0];

        String logoutBody = String.format("{\"token\":\"%s\"}", token);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada correctamente"));
    }

    @Test
    void shouldReturn400WhenLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenLogoutWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token-completamente-invalido-123"))
                .andExpect(status().isUnauthorized());
    }
}
