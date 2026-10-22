package com.vethd.backend.controller;

import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Pet;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.model.User;
import com.vethd.backend.model.Veterinarian;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager em;

    private static final String USER_HEADER = "X-User-Id";
    private static final String FUTURE_DATE = "2026-11-15";
    private static final String PAST_DATE = "2026-01-15";

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
    void shouldCreateAppointmentWithPendingStatus() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "70000001", "Ana", "Perez", "ana1@test.com"), cardio);
        Pet pet = createPet(ownerRole, "70000002", "dueno1@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.reason").value("limping after walk"))
                .andExpect(jsonPath("$.scheduledAt").value(FUTURE_DATE + "T10:00:00"))
                .andExpect(jsonPath("$.pet.id").value(pet.getId().toString()))
                .andExpect(jsonPath("$.veterinarian.id").value(vet.getId().toString()));
    }

    @Test
    void shouldReturn409WhenSlotIsAlreadyBooked() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "71000001", "Ana", "Perez", "ana2@test.com"), cardio);
        Pet pet = createPet(ownerRole, "71000002", "dueno2@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 11, 15, 10, 0));

        em.flush();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenPetDoesNotExist() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "72000001", "Ana", "Perez", "ana3@test.com"), cardio);
        Pet pet = createPet(ownerRole, "72000002", "dueno3@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(UUID.randomUUID(), vet.getId(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenVeterinarianDoesNotExist() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        createVeterinarian(createUser(vetRole, "73000001", "Ana", "Perez", "ana4@test.com"), cardio);
        Pet pet = createPet(ownerRole, "73000002", "dueno4@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), UUID.randomUUID(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenSpecialtyDoesNotExist() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "74000001", "Ana", "Perez", "ana5@test.com"), cardio);
        Pet pet = createPet(ownerRole, "74000002", "dueno5@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), UUID.randomUUID(), FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenPetBelongsToAnotherOwner() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "75000001", "Ana", "Perez", "ana6@test.com"), cardio);
        Pet pet = createPet(ownerRole, "75000002", "dueno6@test.com");
        Pet petFromAnotherOwner = createPet(ownerRole, "75000003", "dueno7@test.com");
        UUID intruderId = petFromAnotherOwner.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, intruderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenUserHeaderIsMissing() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "76000001", "Ana", "Perez", "ana7@test.com"), cardio);
        Pet pet = createPet(ownerRole, "76000002", "dueno8@test.com");

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenHourIsOutsideWorkingHours() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "77000001", "Ana", "Perez", "ana8@test.com"), cardio);
        Pet pet = createPet(ownerRole, "77000002", "dueno9@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "07:00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), FUTURE_DATE, "18:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAppointmentIsInPast() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "78000001", "Ana", "Perez", "ana10@test.com"), cardio);
        Pet pet = createPet(ownerRole, "78000002", "dueno10@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), cardio.getId(), PAST_DATE, "10:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenSpecialtyDoesNotBelongToVeterinarian() throws Exception {
        Role vetRole = createRole("ROLE_VET");
        Role ownerRole = createRole("ROLE_OWNER");
        Specialty cardio = createSpecialty("Cardiologia");
        Specialty derma = createSpecialty("Dermatologia");

        Veterinarian vet = createVeterinarian(
                createUser(vetRole, "79000001", "Ana", "Perez", "ana11@test.com"), cardio);
        Pet pet = createPet(ownerRole, "79000002", "dueno11@test.com");
        UUID ownerId = pet.getOwner().getUser().getId();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), derma.getId(), FUTURE_DATE, "10:00")))
                .andExpect(status().isBadRequest());
    }

    private String appointmentJson(UUID petId, UUID veterinarianId, UUID specialtyId,
                                   String date, String hour) {
        return """
                {
                  "petId": "%s",
                  "veterinarianId": "%s",
                  "specialtyId": "%s",
                  "date": "%s",
                  "hour": "%s",
                  "reason": "limping after walk"
                }
                """.formatted(petId, veterinarianId, specialtyId, date, hour);
    }

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
                .reason("consulta general")
                .build();
        em.persist(appointment);
        return appointment;
    }
}
