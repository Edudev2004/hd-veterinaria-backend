package com.vethd.backend.controller;

import com.vethd.backend.services.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
public class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    // ==========================================
    // 1. ENDPOINTS PÚBLICOS (US-04)
    // ==========================================

    @Test
    @DisplayName("Endpoint público /api/especialidades accesible sin token (HTTP 200)")
    void publicEndpoint_Specialties_NoTokenRequired() throws Exception {
        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint público /api/veterinarios accesible sin token (HTTP 200)")
    void publicEndpoint_Veterinarians_NoTokenRequired() throws Exception {
        mockMvc.perform(get("/api/veterinarios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint público /api/auth/login accesible sin token (no rechaza con 401)")
    void publicEndpoint_AuthLogin_NoTokenRequired() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid@vethd.com\",\"password\":\"password123\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 && result.getResponse().getContentAsString().contains("Token de autenticación faltante")) {
                        throw new AssertionError("El filtro de seguridad no debería bloquear /api/auth/login");
                    }
                });
    }

    // ==========================================
    // 2. TOKEN FALTANTE, INVÁLIDO O REVOCADO (HTTP 401)
    // ==========================================

    @Test
    @DisplayName("Petición a endpoint protegido sin token retorna HTTP 401 Unauthorized en JSON")
    void protectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticación faltante, inválido o expirado"));
    }

    @Test
    @DisplayName("Petición a endpoint protegido con token malformado retorna HTTP 401 Unauthorized")
    void protectedEndpoint_InvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer token_completamente_invalido_12345")
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Petición con token revocado (después de logout) retorna HTTP 401 Unauthorized")
    void protectedEndpoint_RevokedToken_Returns401() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "vet@vethd.com", "VETERINARIO");
        jwtService.invalidateToken(token);

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==========================================
    // 3. AUTORIZACIÓN POR ROL Y HTTP 403 FORBIDDEN
    // ==========================================

    @Test
    @DisplayName("Usuario con rol PROPIETARIO no puede acceder a agenda de veterinario (HTTP 403)")
    void propietario_AccessingVetSchedule_Returns403Forbidden() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "propietario@vethd.com", "PROPIETARIO");

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No tiene permisos para acceder a este recurso"));
    }

    @Test
    @DisplayName("Usuario con rol VETERINARIO puede acceder a agenda de veterinario (HTTP 200)")
    void veterinario_AccessingVetSchedule_Returns200Ok() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "vet@vethd.com", "VETERINARIO");

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario con rol ADMIN puede acceder a agenda de veterinario (HTTP 200)")
    void admin_AccessingVetSchedule_Returns200Ok() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "admin@vethd.com", "ADMIN");

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario con rol PROPIETARIO no puede marcar cita atendida (HTTP 403)")
    void propietario_MarkingAppointmentAttended_Returns403Forbidden() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "cliente@vethd.com", "PROPIETARIO");
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + appointmentId + "/atendida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Usuario con rol VETERINARIO tiene permiso para marcar cita atendida (pasa seguridad)")
    void veterinario_MarkingAppointmentAttended_PassesSecurity() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "veterinario@vethd.com", "VETERINARIO");
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + appointmentId + "/atendida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("El rol VETERINARIO debe tener acceso al endpoint de atender cita");
                    }
                });
    }

    @Test
    @DisplayName("Usuario con rol ADMIN tiene permiso para marcar cita atendida (pasa seguridad)")
    void admin_MarkingAppointmentAttended_PassesSecurity() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "admin@vethd.com", "ADMIN");
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + appointmentId + "/atendida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("El rol ADMIN debe tener acceso al endpoint de atender cita");
                    }
                });
    }

    @Test
    @DisplayName("Usuario con rol VETERINARIO no puede calificar cita (HTTP 403)")
    void veterinario_ReviewingAppointment_Returns403Forbidden() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "vet@vethd.com", "VETERINARIO");
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(post("/api/citas/" + appointmentId + "/valoracion")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5,\"comment\":\"Excelente\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Usuario con rol PROPIETARIO tiene permiso para calificar cita (pasa seguridad)")
    void propietario_ReviewingAppointment_PassesSecurity() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "propietario@vethd.com", "PROPIETARIO");
        UUID appointmentId = UUID.randomUUID();

        mockMvc.perform(post("/api/citas/" + appointmentId + "/valoracion")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5,\"comment\":\"Excelente\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("El rol PROPIETARIO debe tener acceso al endpoint de valoración");
                    }
                });
    }

    @Test
    @DisplayName("Soporte de sinónimos: ROLE_OWNER mapea a PROPIETARIO y no tiene acceso a agenda de vet (HTTP 403)")
    void roleOwner_MapsToPropietario_ForbiddenOnVetSchedule() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "owner@vethd.com", "ROLE_OWNER");

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Soporte de sinónimos: ROLE_VET mapea a VETERINARIO y tiene acceso a agenda de vet (HTTP 200)")
    void roleVet_MapsToVeterinario_AllowedOnVetSchedule() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "vet@vethd.com", "ROLE_VET");

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint de administración /api/admin/** solo es accesible para ADMIN (403 para PROPIETARIO y VETERINARIO)")
    void adminEndpoints_OnlyAccessibleForAdmin() throws Exception {
        String propToken = jwtService.generateToken(UUID.randomUUID(), "prop@vethd.com", "PROPIETARIO");
        String vetToken = jwtService.generateToken(UUID.randomUUID(), "vet@vethd.com", "VETERINARIO");

        mockMvc.perform(get("/api/admin/metrics")
                        .header("Authorization", "Bearer " + propToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/metrics")
                        .header("Authorization", "Bearer " + vetToken))
                .andExpect(status().isForbidden());
    }
}
