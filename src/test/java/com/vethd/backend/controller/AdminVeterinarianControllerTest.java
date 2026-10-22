package com.vethd.backend.controller;

import com.vethd.backend.DTOs.request.VeterinarianCreateRequest;
import com.vethd.backend.DTOs.request.VeterinarianUpdateRequest;
import com.vethd.backend.DTOs.response.AdminVeterinarianResponse;
import com.vethd.backend.services.VeterinarianAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;      
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminVeterinarianControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VeterinarianAdminService veterinarianAdminService;

    @InjectMocks
    private AdminVeterinarianController adminVeterinarianController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminVeterinarianController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private AdminVeterinarianResponse crearRespuestaMock(UUID id, boolean activo) {
        return AdminVeterinarianResponse.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .dni("72123456")
                .firstName("Carlos")
                .lastName("Mendoza")
                .fullName("Carlos Mendoza")
                .email("carlos.vet@vethd.com")
                .active(activo)
                .specialties(List.of("Cirugía"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Debe retornar 200 OK al listar veterinarios")
    void listar_debeRetornar200() throws Exception {
        when(veterinarianAdminService.listar(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/veterinarios")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe retornar 201 Created al registrar un nuevo veterinario")
    void registrar_debeRetornar201() throws Exception {
        UUID id = UUID.randomUUID();
        AdminVeterinarianResponse response = crearRespuestaMock(id, true);

        when(veterinarianAdminService.registrar(any(VeterinarianCreateRequest.class)))
                .thenReturn(response);

        String jsonValido = """
        {
            "dni": "72123456",
            "firstName": "Carlos",
            "lastName": "Mendoza",
            "email": "carlos.vet@vethd.com",
            "password": "Password123!",
            "specialtyIds": []
        }
        """;

        mockMvc.perform(post("/api/admin/veterinarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Debe retornar 200 OK al editar veterinario")
    void editar_debeRetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        AdminVeterinarianResponse response = crearRespuestaMock(id, true);

        when(veterinarianAdminService.editar(eq(id), any(VeterinarianUpdateRequest.class)))
                .thenReturn(response);

        String jsonValido = """
        {
            "dni": "72123456",
            "firstName": "Carlos",
            "lastName": "Mendoza",
            "email": "carlos.vet@vethd.com",
            "specialtyIds": []
        }
        """;

        mockMvc.perform(put("/api/admin/veterinarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe retornar 200 OK al alternar estado activo/inactivo (Toggle)")
    void cambiarEstado_debeRetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        AdminVeterinarianResponse response = crearRespuestaMock(id, false);

        when(veterinarianAdminService.cambiarEstado(eq(id)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/admin/veterinarios/" + id + "/estado"))
                .andExpect(status().isOk());
    }
}