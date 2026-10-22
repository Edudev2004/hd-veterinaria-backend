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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private static final String REASON = "limping after walk";

    private static final String[] TABLAS = {
            "veterinarian_specialties", "reviews", "appointments", "pets",
            "owners", "veterinarians", "specialties", "users", "roles"
    };

    private Role vetRole;
    private Role ownerRole;
    private Specialty cardio;
    private Veterinarian vet;
    private Pet pet;
    private UUID ownerId;

    @BeforeEach
    void cleanDatabase() {
        for (String tabla : TABLAS) {
            em.createNativeQuery("DELETE FROM " + tabla).executeUpdate();
        }
    }

    private void setupScenario() {
        vetRole = createRole("ROLE_VET");
        ownerRole = createRole("ROLE_OWNER");
        cardio = createSpecialty("Cardiologia");
        vet = createVeterinarian(createUser(vetRole, "70000001", "Ana", "Perez", "ana@test.com"), cardio);
        pet = createPet(ownerRole, "70000002", "dueno@test.com");
        ownerId = pet.getOwner().getUser().getId();
        em.flush();
    }

    // ---------------------------------------------------------------
    // US-14 - AGENDAR CITA
    // ---------------------------------------------------------------

    @Test
    void shouldCreateAppointmentWithPendingStatus() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.reason").value(REASON))
                .andExpect(jsonPath("$.scheduledAt").value(FUTURE_DATE + "T10:00:00"))
                .andExpect(jsonPath("$.pet.id").value(pet.getId().toString()))
                .andExpect(jsonPath("$.veterinarian.id").value(vet.getId().toString()));
    }

    @Test
    void shouldReturn409WhenSlotIsAlreadyBooked() throws Exception {
        setupScenario();
        createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 11, 15, 10, 0));
        em.flush();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenPetDoesNotExist() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(UUID.randomUUID(), vet.getId(), cardio.getId(),
                                FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenVeterinarianDoesNotExist() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), UUID.randomUUID(), cardio.getId(),
                                FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenSpecialtyDoesNotExist() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), UUID.randomUUID(),
                                FUTURE_DATE, "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenPetBelongsToAnotherOwner() throws Exception {
        setupScenario();
        UUID intruderId = createPet(ownerRole, "70000003", "otro-dueno@test.com")
                .getOwner().getUser().getId();
        em.flush();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, intruderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenUserHeaderIsMissing() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenHourIsOutsideWorkingHours() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "07:00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "18:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAppointmentIsInPast() throws Exception {
        setupScenario();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(PAST_DATE, "10:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenSpecialtyDoesNotBelongToVeterinarian() throws Exception {
        setupScenario();
        Specialty derma = createSpecialty("Dermatologia");
        em.flush();

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(pet.getId(), vet.getId(), derma.getId(),
                                FUTURE_DATE, "10:00")))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // US-15 - REPROGRAMAR CITA
    // ---------------------------------------------------------------

    @Test
    void shouldRescheduleAppointmentToNewSlot() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "15:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointment.getId().toString()))
                .andExpect(jsonPath("$.scheduledAt").value(FUTURE_DATE + "T15:00:00"))
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    @Test
    void shouldReturn400WhenNewVeterinarianDoesNotHaveSpecialty() throws Exception {
        setupScenario();
        Specialty derma = createSpecialty("Dermatologia");
        em.flush();
        Veterinarian newVet = createVeterinarian(
                createUser(vetRole, "70000011", "Luis", "Soto", "luis@test.com"), derma);
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "15:00", newVet.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldMoveAppointmentToVeterinarianWithSameSpecialty() throws Exception {
        setupScenario();
        Veterinarian newVet = createVeterinarian(
                createUser(vetRole, "70000012", "Luis", "Soto", "luis2@test.com"), cardio);
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "15:00", newVet.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veterinarian.id").value(newVet.getId().toString()));
    }

    @Test
    void shouldRescheduleToSameSlotWithoutConflict() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledAt").value(FUTURE_DATE + "T10:00:00"));
    }

    @Test
    void shouldReturn409WhenNewSlotIsAlreadyBooked() throws Exception {
        setupScenario();
        createAppointment(pet, vet, cardio, LocalDateTime.of(2026, 11, 15, 10, 0));
        Appointment appointmentToMove = pendingAppointment(LocalDateTime.of(2026, 11, 15, 15, 0));

        mockMvc.perform(patch("/api/citas/" + appointmentToMove.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenReschedulingNonPendingAppointment() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));
        appointment.setStatus("CANCELADA");

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "15:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403WhenReschedulingAnotherOwnersAppointment() throws Exception {
        setupScenario();
        UUID intruderId = createPet(ownerRole, "70000004", "otro-dueno2@test.com")
                .getOwner().getUser().getId();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/reprogramar")
                        .header(USER_HEADER, intruderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rescheduleJson(FUTURE_DATE, "15:00")))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // US-16 - CANCELAR CITA
    // ---------------------------------------------------------------

    @Test
    void shouldCancelAppointmentWithReason() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/cancelar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelJson("owner is travelling")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointment.getId().toString()))
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.cancellationReason").value("owner is travelling"))
                .andExpect(jsonPath("$.scheduledAt").value(FUTURE_DATE + "T10:00:00"))
                .andExpect(jsonPath("$.reason").value(REASON));
    }

    @Test
    void shouldReturn400WhenCancellingNonPendingAppointment() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));
        appointment.setStatus("ATENDIDA");

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/cancelar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelJson("no aplica")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCancellationReasonIsBlank() throws Exception {
        setupScenario();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/cancelar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelJson("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403WhenCancellingAnotherOwnersAppointment() throws Exception {
        setupScenario();
        UUID intruderId = createPet(ownerRole, "70000005", "otro-dueno3@test.com")
                .getOwner().getUser().getId();
        Appointment appointment = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + appointment.getId() + "/cancelar")
                        .header(USER_HEADER, intruderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelJson("motivo ajeno")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldBookSlotAgainAfterCancellation() throws Exception {
        setupScenario();
        Appointment first = pendingAppointment(LocalDateTime.of(2026, 11, 15, 10, 0));

        mockMvc.perform(patch("/api/citas/" + first.getId() + "/cancelar")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelJson("ya no viene")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/citas")
                        .header(USER_HEADER, ownerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appointmentJson(FUTURE_DATE, "10:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------

    private Appointment pendingAppointment(LocalDateTime scheduledAt) {
        Appointment appointment = createAppointment(pet, vet, cardio, scheduledAt);
        em.flush();
        return appointment;
    }

    private String appointmentJson(String date, String hour) {
        return appointmentJson(pet.getId(), vet.getId(), cardio.getId(), date, hour);
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
                  "reason": "%s"
                }
                """.formatted(petId, veterinarianId, specialtyId, date, hour, REASON);
    }

    private String rescheduleJson(String date, String hour) {
        return """
                {
                  "newDate": "%s",
                  "newHour": "%s"
                }
                """.formatted(date, hour);
    }

    private String rescheduleJson(String date, String hour, UUID newVeterinarianId) {
        return """
                {
                  "newDate": "%s",
                  "newHour": "%s",
                  "newVeterinarianId": "%s"
                }
                """.formatted(date, hour, newVeterinarianId);
    }

    private String cancelJson(String cancellationReason) {
        return """
                {
                  "cancellationReason": "%s"
                }
                """.formatted(cancellationReason);
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
                .reason(REASON)
                .build();
        em.persist(appointment);
        return appointment;
    }
}
