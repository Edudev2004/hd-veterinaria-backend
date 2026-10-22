package com.vethd.backend.controller;

import com.vethd.backend.model.Owner;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
