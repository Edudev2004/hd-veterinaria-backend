package com.vethd.backend.controller;

import com.vethd.backend.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class PetHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager em;

    private static final String USER_HEADER = "X-User-Id";

    private static final String[] TABLAS = {
            "veterinarian_specialties", "reviews", "diagnoses", "appointments", "pets",
            "owners", "veterinarians", "specialties", "users", "roles"
    };

    private Role vetRole;
    private Role ownerRole;
    private Role adminRole;
    private Specialty cardio;
    private Veterinarian vet;
    private Pet pet;
    private UUID ownerId;
    private UUID vetUserId;
    private UUID adminUserId;

    @BeforeEach
    void cleanDatabase() {
        for (String tabla : TABLAS) {
            em.createNativeQuery("DELETE FROM " + tabla).executeUpdate();
        }
    }

    private void setupScenario() {
        vetRole = createRole("ROLE_VET");
        ownerRole = createRole("ROLE_OWNER");
        adminRole = createRole("ROLE_ADMIN");

        cardio = createSpecialty("Cardiologia");

        User vetUser = createUser(vetRole, "70000001", "Ana", "Perez", "ana@test.com");
        vet = createVeterinarian(vetUser, cardio);
        vetUserId = vetUser.getId();

        pet = createPet(ownerRole, "70000002", "dueno@test.com");
        ownerId = pet.getOwner().getUser().getId();

        User adminUser = createUser(adminRole, "70000099", "Admin", "General", "admin@test.com");
        adminUserId = adminUser.getId();

        em.flush();
    }

    @Test
    void shouldGetPetMedicalHistoryForOwner() throws Exception {
        setupScenario();
        Appointment appointment = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 3, 15, 9, 30));
        createDiagnosis(appointment, "Soplo cardiaco grado I", "Peso: 28kg, Temp: 38.4", "Enalapril 5mg", "Control en 6 meses");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pet.id").value(pet.getId().toString()))
                .andExpect(jsonPath("$.pet.name").value("Toby"))
                .andExpect(jsonPath("$.totalAtenciones").value(1))
                .andExpect(jsonPath("$.entradas.length()").value(1))
                .andExpect(jsonPath("$.entradas[0].diagnosis").value("Soplo cardiaco grado I"))
                .andExpect(jsonPath("$.entradas[0].treatment").value("Enalapril 5mg"))
                .andExpect(jsonPath("$.entradas[0].details").value("Peso: 28kg, Temp: 38.4"))
                .andExpect(jsonPath("$.entradas[0].notes").value("Control en 6 meses"))
                .andExpect(jsonPath("$.entradas[0].veterinarian.name").value("Ana Perez"));
    }

    @Test
    void shouldReturnEmptyHistoryWhenPetHasNoDiagnoses() throws Exception {
        setupScenario();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pet.id").value(pet.getId().toString()))
                .andExpect(jsonPath("$.totalAtenciones").value(0))
                .andExpect(jsonPath("$.entradas.length()").value(0));
    }

    @Test
    void shouldAllowVeterinarianToAccessAnyPetMedicalHistory() throws Exception {
        setupScenario();
        Appointment appointment = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 3, 15, 9, 30));
        createDiagnosis(appointment, "Chequeo general", "Todo normal", "Sin medicacion", "Vacunas al dia");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, vetUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtenciones").value(1))
                .andExpect(jsonPath("$.entradas[0].diagnosis").value("Chequeo general"));
    }

    @Test
    void shouldAllowAdminToAccessAnyPetMedicalHistory() throws Exception {
        setupScenario();
        Appointment appointment = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 3, 15, 9, 30));
        createDiagnosis(appointment, "Chequeo admin", "Todo normal", "Sin medicacion", "Ninguna");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, adminUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtenciones").value(1));
    }

    @Test
    void shouldFilterHistoryBySearchTerm() throws Exception {
        setupScenario();
        Appointment a1 = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 3, 15, 9, 30));
        createDiagnosis(a1, "Dermatitis atopica", "Piel roja", "Apoquel", "Evitar humedad");

        Appointment a2 = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 5, 20, 10, 0));
        createDiagnosis(a2, "Gastroenteritis aguda", "Vomito", "Omeprazol", "Dieta blanda");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, ownerId.toString())
                        .param("busqueda", "Derma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtenciones").value(1))
                .andExpect(jsonPath("$.entradas[0].diagnosis").value("Dermatitis atopica"));
    }

    @Test
    void shouldFilterHistoryByDateRange() throws Exception {
        setupScenario();
        Appointment a1 = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 1, 10, 9, 30));
        createDiagnosis(a1, "Diagnostico Enero", "Datos 1", "Tratamiento 1", "Notas 1");

        Appointment a2 = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 6, 15, 10, 0));
        createDiagnosis(a2, "Diagnostico Junio", "Datos 2", "Tratamiento 2", "Notas 2");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, ownerId.toString())
                        .param("fechaDesde", "2026-05-01")
                        .param("fechaHasta", "2026-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtenciones").value(1))
                .andExpect(jsonPath("$.entradas[0].diagnosis").value("Diagnostico Junio"));
    }

    @Test
    void shouldGetEntriesDirectlyViaSubendpoint() throws Exception {
        setupScenario();
        Appointment appointment = createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 3, 15, 9, 30));
        createDiagnosis(appointment, "Diagnostico directo", "Datos", "Tratamiento", "Notas");
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial/entradas")
                        .header(USER_HEADER, ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diagnosis").value("Diagnostico directo"));
    }

    @Test
    void shouldReturn404WhenPetNotFound() throws Exception {
        setupScenario();

        mockMvc.perform(get("/api/mascotas/" + UUID.randomUUID() + "/historial")
                        .header(USER_HEADER, ownerId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenOtherOwnerTriesToAccessPetHistory() throws Exception {
        setupScenario();
        UUID intruderId = createPet(ownerRole, "70000009", "otro-dueno-historial@test.com")
                .getOwner().getUser().getId();
        em.flush();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial")
                        .header(USER_HEADER, intruderId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenNoAuthenticationProvided() throws Exception {
        setupScenario();

        mockMvc.perform(get("/api/mascotas/" + pet.getId() + "/historial"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------

    private Role createRole(String name) {
        Role role = Role.builder().name(name).description("test role").build();
        em.persist(role);
        return role;
    }

    private User createUser(Role role, String dni, String firstName, String lastName, String email) {
        User user = User.builder()
                .dni(dni)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash("test-hash")
                .role(role)
                .build();
        em.persist(user);
        return user;
    }

    private Specialty createSpecialty(String name) {
        Specialty specialty = Specialty.builder().name(name).description("test specialty").build();
        em.persist(specialty);
        return specialty;
    }

    private Veterinarian createVeterinarian(User user, Specialty... specialties) {
        Veterinarian veterinarian = Veterinarian.builder().user(user).build();
        veterinarian.getSpecialties().addAll(java.util.List.of(specialties));
        em.persist(veterinarian);
        return veterinarian;
    }

    private Pet createPet(Role ownerRole, String ownerDni, String ownerEmail) {
        User ownerUser = createUser(ownerRole, ownerDni, "Due", "No", ownerEmail);
        Owner owner = Owner.builder().user(ownerUser).phone("999999999").address("Calle 1").build();
        em.persist(owner);

        Pet pet = Pet.builder()
                .owner(owner)
                .name("Toby")
                .species("Perro")
                .gender("Macho")
                .build();
        em.persist(pet);
        return pet;
    }

    private Appointment createAppointment(Pet pet, Veterinarian vet, Specialty specialty,
                                          LocalDateTime scheduledAt) {
        Appointment appointment = Appointment.builder()
                .pet(pet)
                .veterinarian(vet)
                .specialty(specialty)
                .scheduledAt(scheduledAt)
                .reason("Consulta medica")
                .status("ATENDIDA")
                .build();
        em.persist(appointment);
        return appointment;
    }

    private Diagnosis createDiagnosis(Appointment appointment, String diag, String details, String treatment, String notes) {
        Diagnosis diagnosis = Diagnosis.builder()
                .appointment(appointment)
                .diagnosis(diag)
                .details(details)
                .treatment(treatment)
                .notes(notes)
                .nextSuggestedAppointment(LocalDate.now().plusMonths(3))
                .build();
        em.persist(diagnosis);
        return diagnosis;
    }
}

