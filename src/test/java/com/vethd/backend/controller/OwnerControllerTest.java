package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final UUID PROPIETARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private OwnerController ownerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    private OwnerResponse buildResponse() {
        return OwnerResponse.builder()
                .id(PROPIETARIO_DEMO_ID)
                .firstName("Carlos")
                .lastName("Perez")
                .email("carlos@vethd.com")
                .phone("987654321")
                .address("Av. Lima 123")
                .build();
    }

    @Test
    void getMeDevuelveElPerfilDelPropietario() throws Exception {
        when(ownerService.getProfile(PROPIETARIO_DEMO_ID)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/propietarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.email").value("carlos@vethd.com"));
    }

    @Test
    void putMeActualizaYDevuelveElPerfil() throws Exception {
        when(ownerService.updateProfile(eq(PROPIETARIO_DEMO_ID), any())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/propietarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Carlos\",\"phone\":\"987654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("987654321"));
    }

    @Test
    void putMeRechazaUnTelefonoMuyCortoConBadRequest() throws Exception {
        mockMvc.perform(put("/api/propietarios/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest());

        verify(ownerService, never()).updateProfile(any(), any());
    }
}
