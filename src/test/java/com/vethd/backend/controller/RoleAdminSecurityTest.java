
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
class RoleAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("US-30: Sin token no puede listar roles")
    void rolesSinTokenRetorna401() throws Exception {

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("US-30: PROPIETARIO no puede listar roles")
    void propietarioNoPuedeListarRoles() throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "owner@vethd.com",
                "PROPIETARIO"
        );

        mockMvc.perform(get("/api/admin/roles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-30: VETERINARIO no puede asignar roles")
    void veterinarioNoPuedeAsignarRoles() throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "vet@vethd.com",
                "VETERINARIO"
        );

        mockMvc.perform(
                patch("/api/admin/usuarios/"
                        + UUID.randomUUID() + "/rol")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US-30: ADMIN puede consultar roles")
    void adminPuedeListarRoles() throws Exception {

        String token = jwtService.generateToken(
                UUID.randomUUID(),
                "admin@vethd.com",
                "ADMIN"
        );

        mockMvc.perform(get("/api/admin/roles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
