
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    // 1. ENDPOINTS PUBLICOS
    // ==========================================

    @Test
    @DisplayName("Endpoint publico de especialidades HTTP 200")
    void publicEndpoint_Specialties_NoTokenRequired()
            throws Exception {

        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint publico de veterinarios HTTP 200")
    void publicEndpoint_Veterinarians_NoTokenRequired()
            throws Exception {

        mockMvc.perform(get("/api/veterinarios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login publico sin token")
    void publicEndpoint_AuthLogin_NoTokenRequired()
            throws Exception {

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "invalid@vethd.com",
                      "password": "password123"
                    }
                    """))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();

                    if (code == 401 &&
                        result.getResponse()
                              .getContentAsString()
                              .contains("Token de autenticación faltante")) {

                        throw new AssertionError(
                            "El login no debe exigir JWT"
                        );
                    }
                });
    }

    // ==========================================
    // 2. AUTENTICACION HTTP 401
    // ==========================================

    @Test
    @DisplayName("Endpoint protegido sin token retorna 401")
    void protectedEndpoint_WithoutToken_Returns401()
            throws Exception {

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                    .value("Token de autenticación faltante, inválido o expirado"));
    }

    @Test
    @DisplayName("Token invalido retorna HTTP 401")
    void protectedEndpoint_InvalidToken_Returns401()
            throws Exception {

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header(
                    "Authorization",
                    "Bearer token_completamente_invalido_12345"
                )
                .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Token revocado retorna HTTP 401")
    void protectedEndpoint_RevokedToken_Returns401()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        jwtService.invalidateToken(token);

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==========================================
    // 3. AUTORIZACION POR ROLES
    // ==========================================

    @Test
    @DisplayName("PROPIETARIO no puede consultar agenda veterinaria")
    void propietario_AccessingVetSchedule_Returns403Forbidden()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "propietario@vethd.com",
                "PROPIETARIO"
        );

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message")
                    .value("No tiene permisos para acceder a este recurso"));
    }

    @Test
    @DisplayName("VETERINARIO puede consultar su agenda")
    void veterinario_AccessingVetSchedule_Returns200Ok()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN puede consultar agenda veterinaria")
    void admin_AccessingVetSchedule_Returns200Ok()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "admin@vethd.com",
                "ADMIN"
        );

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PROPIETARIO no puede marcar cita atendida")
    void propietario_MarkingAppointmentAttended_Returns403Forbidden()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "cliente@vethd.com",
                "PROPIETARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + id + "/atendida")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("VETERINARIO pasa seguridad para atender citas")
    void veterinario_MarkingAppointmentAttended_PassesSecurity()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "veterinario@vethd.com",
                "VETERINARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + id + "/atendida")
                .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();

                    if (code == 401 || code == 403) {
                        throw new AssertionError(
                            "VETERINARIO debe pasar seguridad"
                        );
                    }
                });
    }

    @Test
    @DisplayName("ADMIN pasa seguridad para atender citas")
    void admin_MarkingAppointmentAttended_PassesSecurity()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "admin@vethd.com",
                "ADMIN"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/citas/" + id + "/atendida")
                .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();

                    if (code == 401 || code == 403) {
                        throw new AssertionError(
                            "ADMIN debe pasar seguridad"
                        );
                    }
                });
    }

    @Test
    @DisplayName("VETERINARIO no puede valorar citas")
    void veterinario_ReviewingAppointment_Returns403Forbidden()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/citas/" + id + "/valoracion")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "score": 5,
                      "comment": "Excelente"
                    }
                    """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PROPIETARIO pasa seguridad para valorar citas")
    void propietario_ReviewingAppointment_PassesSecurity()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "propietario@vethd.com",
                "PROPIETARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/citas/" + id + "/valoracion")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "score": 5,
                      "comment": "Excelente"
                    }
                    """))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();

                    if (code == 401 || code == 403) {
                        throw new AssertionError(
                            "PROPIETARIO debe pasar seguridad"
                        );
                    }
                });
    }

    @Test
    @DisplayName("ROLE_OWNER equivale a PROPIETARIO")
    void roleOwner_MapsToPropietario_ForbiddenOnVetSchedule()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "owner@vethd.com",
                "ROLE_OWNER"
        );

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("ROLE_VET equivale a VETERINARIO")
    void roleVet_MapsToVeterinario_AllowedOnVetSchedule()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "ROLE_VET"
        );

        mockMvc.perform(get("/api/veterinarios/me/agenda")
                .header("Authorization", "Bearer " + token)
                .param("fecha", "2026-10-22"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints admin restringidos para otros roles")
    void adminEndpoints_OnlyAccessibleForAdmin()
            throws Exception {

        String propToken = jwtService.generateToken(
                UUID.randomUUID(),
                "prop@vethd.com",
                "PROPIETARIO"
        );

        String vetToken = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        mockMvc.perform(get("/api/admin/metrics")
                .header("Authorization", "Bearer " + propToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/metrics")
                .header("Authorization", "Bearer " + vetToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-29 GET admin sin token retorna 401")
    void specialtiesAdmin_WithoutToken_Returns401()
            throws Exception {

        mockMvc.perform(get("/api/admin/especialidades"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("US-29 propietario no puede listar especialidades")
    void specialtiesAdmin_Owner_Returns403()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "owner@vethd.com",
                "PROPIETARIO"
        );

        mockMvc.perform(get("/api/admin/especialidades")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-29 propietario no puede crear especialidades")
    void specialtiesAdmin_OwnerCannotCreate_Returns403()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "owner@vethd.com",
                "PROPIETARIO"
        );

        mockMvc.perform(post("/api/admin/especialidades")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Cardiologia",
                      "description": "Atencion cardiaca"
                    }
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-29 veterinario no puede editar especialidades")
    void specialtiesAdmin_VetCannotUpdate_Returns403()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/admin/especialidades/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Dermatologia",
                      "description": "Atencion dermatologica"
                    }
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-29 veterinario no puede cambiar estado")
    void specialtiesAdmin_VetCannotChangeStatus_Returns403()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                patch("/api/admin/especialidades/" + id + "/estado")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "active": false
                    }
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-29 ADMIN puede listar especialidades")
    void specialtiesAdmin_AdminCanList_Returns200()
            throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "admin@vethd.com",
                "ADMIN"
        );

        mockMvc.perform(get("/api/admin/especialidades")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}


