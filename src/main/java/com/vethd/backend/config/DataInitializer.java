package com.vethd.backend.config;

import com.vethd.backend.model.*;
import com.vethd.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[DATA_INITIALIZER] Iniciando verificación y siembra de datos iniciales...");

        Role rolePropietario = getOrCreateRole("ROLE_PROPIETARIO", "PROPIETARIO", "Rol de propietario de mascotas");
        Role roleVeterinario = getOrCreateRole("ROLE_VETERINARIO", "VETERINARIO", "Rol de médico veterinario");
        Role roleAdmin = getOrCreateRole("ROLE_ADMIN", "ADMIN", "Rol de administrador del sistema");

        Specialty specialty = specialtyRepository.findByNameIgnoreCase("Medicina General")
                .orElseGet(() -> specialtyRepository.save(Specialty.builder()
                        .name("Medicina General")
                        .description("Atención médica veterinaria general y preventiva")
                        .active(true)
                        .build()));

        // 1. Propietario demo solicitado
        String emailProp = "propietario@vethd.com";
        if (userRepository.findByEmail(emailProp).isEmpty()) {
            User userProp = userRepository.save(User.builder()
                    .dni("10000001")
                    .firstName("Carlos")
                    .lastName("Propietario")
                    .email(emailProp)
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .role(rolePropietario)
                    .active(true)
                    .build());

            Owner owner = ownerRepository.save(Owner.builder()
                    .user(userProp)
                    .phone("987654321")
                    .address("Av. Las Palmeras 450, Lima")
                    .build());

            petRepository.save(Pet.builder()
                    .owner(owner)
                    .name("Firulais")
                    .species("Perro")
                    .breed("Golden Retriever")
                    .gender("Macho")
                    .birthDate(LocalDate.now().minusYears(3))
                    .observations("Vacunas al día. Muy amigable.")
                    .active(true)
                    .build());

            log.info("[DATA_INITIALIZER] Usuario Propietario creado: {} / 12345678", emailProp);
        } else {
            User userProp = userRepository.findByEmail(emailProp).get();
            userProp.setPasswordHash(passwordEncoder.encode("12345678"));
            userRepository.save(userProp);
        }

        // 2. Veterinario demo solicitado
        String emailVet = "veterinario@vethd.com";
        if (userRepository.findByEmail(emailVet).isEmpty()) {
            User userVet = userRepository.save(User.builder()
                    .dni("20000002")
                    .firstName("Laura")
                    .lastName("Veterinario")
                    .email(emailVet)
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .role(roleVeterinario)
                    .active(true)
                    .build());

            veterinarianRepository.save(Veterinarian.builder()
                    .user(userVet)
                    .active(true)
                    .specialties(Set.of(specialty))
                    .build());

            log.info("[DATA_INITIALIZER] Usuario Veterinario creado: {} / 12345678", emailVet);
        } else {
            User userVet = userRepository.findByEmail(emailVet).get();
            userVet.setPasswordHash(passwordEncoder.encode("12345678"));
            userRepository.save(userVet);
        }

        // 3. Admin demo solicitado
        String emailAdmin = "admin@vethd.com";
        if (userRepository.findByEmail(emailAdmin).isEmpty()) {
            userRepository.save(User.builder()
                    .dni("30000003")
                    .firstName("Administrador")
                    .lastName("VetHD")
                    .email(emailAdmin)
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .role(roleAdmin)
                    .active(true)
                    .build());

            log.info("[DATA_INITIALIZER] Usuario Administrador creado: {} / 12345678", emailAdmin);
        } else {
            User userAdmin = userRepository.findByEmail(emailAdmin).get();
            userAdmin.setPasswordHash(passwordEncoder.encode("12345678"));
            userRepository.save(userAdmin);
        }

        log.info("[DATA_INITIALIZER] Verificación de datos completada exitosamente.");
    }

    private Role getOrCreateRole(String rolePrefixed, String roleSimple, String description) {
        return roleRepository.findByName(rolePrefixed)
                .or(() -> roleRepository.findByName(roleSimple))
                .or(() -> roleRepository.findByNameIgnoreCase(roleSimple))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(rolePrefixed)
                        .description(description)
                        .build()));
    }
}
