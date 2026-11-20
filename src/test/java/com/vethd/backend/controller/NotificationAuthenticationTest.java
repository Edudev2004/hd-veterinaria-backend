package com.vethd.backend.controller;

import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Notification;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Pet;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.model.User;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.services.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Transactional
class NotificationAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @PersistenceContext
    private EntityManager em;

    private static final String[] TABLES = {
            "notifications", "veterinarian_specialties", "reviews", "diagnoses", "appointments", "pets",
            "owners", "veterinarians", "specialties", "users", "roles"
    };

    @BeforeEach
    void cleanDatabase() {
        for (String table : TABLES) {
            em.createNativeQuery("DELETE FROM " + table).executeUpdate();
        }
    }

    @Test
    void forgedUserIdCannotAccessNotificationsOrRemindersWithoutJwt() throws Exception {
        String forgedUserId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/notificaciones").header("X-User-Id", forgedUserId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/recordatorios").header("X-User-Id", forgedUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validJwtRestrictsNotificationsAndRemindersToItsOwner() throws Exception {
        Role ownerRole = createRole("ROLE_OWNER");
        Role vetRole = createRole("ROLE_VET");
        Specialty specialty = createSpecialty();
        Veterinarian veterinarian = createVeterinarian(
                createUser(vetRole, "81000001", "Ana", "Perez", "ana@test.com"),
                specialty
        );
        Pet ownerPet = createPet(ownerRole, "81000002", "owner@test.com");
        Pet otherPet = createPet(ownerRole, "81000003", "other@test.com");

        LocalDateTime now = LocalDateTime.now().withNano(0);
        createAppointment(ownerPet, veterinarian, specialty, now.plusHours(1));
        createAppointment(otherPet, veterinarian, specialty, now.plusHours(1));
        createNotification(ownerPet.getOwner().getUser(), "Notificación propia");
        createNotification(otherPet.getOwner().getUser(), "Notificación ajena");
        em.flush();

        User owner = ownerPet.getOwner().getUser();
        String token = jwtService.generateToken(owner.getId(), owner.getEmail(), "ROLE_OWNER");
        String otherUserId = otherPet.getOwner().getUser().getId().toString();

        mockMvc.perform(get("/api/notificaciones")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", otherUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("Notificación propia"));

        mockMvc.perform(get("/api/recordatorios")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", otherUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pet.id").value(ownerPet.getId().toString()));
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

    private Specialty createSpecialty() {
        Specialty specialty = Specialty.builder().name("Medicina General").description("Consultas").build();
        em.persist(specialty);
        return specialty;
    }

    private Veterinarian createVeterinarian(User user, Specialty specialty) {
        Veterinarian veterinarian = Veterinarian.builder().user(user).build();
        veterinarian.getSpecialties().add(specialty);
        em.persist(veterinarian);
        return veterinarian;
    }

    private Pet createPet(Role role, String dni, String email) {
        User user = createUser(role, dni, "Due", "No", email);
        Owner owner = Owner.builder().user(user).phone("999999999").address("Calle 1").build();
        em.persist(owner);
        Pet pet = Pet.builder().owner(owner).name("Toby").species("Perro").gender("Macho").build();
        em.persist(pet);
        return pet;
    }

    private void createNotification(User user, String message) {
        em.persist(Notification.builder().user(user).title("Aviso").message(message).build());
    }

    private void createAppointment(Pet pet, Veterinarian veterinarian, Specialty specialty, LocalDateTime at) {
        em.persist(Appointment.builder()
                .pet(pet)
                .veterinarian(veterinarian)
                .specialty(specialty)
                .scheduledAt(at)
                .status("PENDIENTE")
                .reason("Consulta médica")
                .build());
    }
}
