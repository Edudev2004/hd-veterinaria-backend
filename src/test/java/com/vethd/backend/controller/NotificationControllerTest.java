package com.vethd.backend.controller;

import com.vethd.backend.model.Appointment;
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
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @PersistenceContext
    private EntityManager em;

    private static final String USER_HEADER = "X-User-Id";

    private static final String[] TABLES = {
            "veterinarian_specialties", "reviews", "diagnoses", "appointments", "pets",
            "owners", "veterinarians", "specialties", "users", "roles"
    };

    @BeforeEach
    void cleanDatabase() {
        for (String table : TABLES) {
            em.createNativeQuery("DELETE FROM " + table).executeUpdate();
        }
    }

    @Test
    void remindersContainOnlyOwnersUpcomingActiveAppointmentsWithin24Hours() throws Exception {
        Role ownerRole = createRole("ROLE_OWNER");
        Role vetRole = createRole("ROLE_VET");
        Specialty specialty = createSpecialty();
        User vetUser = createUser(vetRole, "80000001", "Ana", "Perez");
        Veterinarian veterinarian = createVeterinarian(vetUser, specialty);
        Pet ownerPet = createPet(ownerRole, "80000002", "owner@test.com");
        Pet anotherOwnerPet = createPet(ownerRole, "80000003", "another-owner@test.com");

        LocalDateTime now = LocalDateTime.now().withNano(0);
        Appointment reminder = createAppointment(ownerPet, veterinarian, specialty, now.plusHours(1), "PENDIENTE");
        createAppointment(ownerPet, veterinarian, specialty, now.plusHours(2), "CANCELADA");
        createAppointment(ownerPet, veterinarian, specialty, now.plusHours(3), "COMPLETADA");
        createAppointment(ownerPet, veterinarian, specialty, now.plusHours(25), "PENDIENTE");
        createAppointment(anotherOwnerPet, veterinarian, specialty, now.plusHours(2), "PENDIENTE");
        em.flush();

        User owner = ownerPet.getOwner().getUser();
        String token = jwtService.generateToken(owner.getId(), owner.getEmail(), "ROLE_OWNER");
        mockMvc.perform(get("/api/recordatorios")
                        .header("Authorization", "Bearer " + token)
                        .header(USER_HEADER, anotherOwnerPet.getOwner().getUser().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reminder.getId().toString()))
                .andExpect(jsonPath("$[0].pet.name").value(ownerPet.getName()));
    }

    private Role createRole(String name) {
        Role role = Role.builder().name(name).description("test role").build();
        em.persist(role);
        return role;
    }

    private User createUser(Role role, String dni, String firstName, String lastName) {
        User user = User.builder()
                .dni(dni)
                .firstName(firstName)
                .lastName(lastName)
                .email(dni + "@test.com")
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
        User ownerUser = createUser(role, dni, "Due", "No");
        ownerUser.setEmail(email);
        Owner owner = Owner.builder().user(ownerUser).phone("999999999").address("Calle 1").build();
        em.persist(owner);
        Pet pet = Pet.builder().owner(owner).name("Toby").species("Perro").gender("Macho").build();
        em.persist(pet);
        return pet;
    }

    private Appointment createAppointment(
            Pet pet,
            Veterinarian veterinarian,
            Specialty specialty,
            LocalDateTime scheduledAt,
            String status
    ) {
        Appointment appointment = Appointment.builder()
                .pet(pet)
                .veterinarian(veterinarian)
                .specialty(specialty)
                .scheduledAt(scheduledAt)
                .status(status)
                .reason("Consulta médica")
                .build();
        em.persist(appointment);
        return appointment;
    }
}
