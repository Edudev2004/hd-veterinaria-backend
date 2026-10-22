package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.services.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private static final UUID PROPIETARIO_DEMO_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final String BODY_VALIDO =
            "{\"name\":\"Firulais\",\"species\":\"perro\",\"breed\":\"Labrador\",\"gender\":\"macho\",\"birthDate\":\"2024-03-15\"}";

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();
    }

    private PetResponse buildResponse(UUID id, String name) {
        return PetResponse.builder()
                .id(id)
                .name(name)
                .species("perro")
                .gender("macho")
                .active(true)
                .build();
    }

    @Test
    void postRegistraLaMascotaYDevuelve201ConSuId() throws Exception {
        UUID id = UUID.randomUUID();
        when(petService.register(eq(PROPIETARIO_DEMO_ID), any())).thenReturn(buildResponse(id, "Firulais"));

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Firulais"));
    }

    @Test
    void postSinCamposObligatoriosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"breed\":\"Labrador\"}"))
                .andExpect(status().isBadRequest());

        verify(petService, never()).register(any(), any());
    }

    @Test
    void getListaLasMascotasDelPropietario() throws Exception {
        when(petService.listByOwner(PROPIETARIO_DEMO_ID))
                .thenReturn(List.of(buildResponse(UUID.randomUUID(), "Luna"), buildResponse(UUID.randomUUID(), "Max")));

        mockMvc.perform(get("/api/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Luna"));
    }

    @Test
    void putActualizaLaMascota() throws Exception {
        UUID id = UUID.randomUUID();
        when(petService.update(eq(PROPIETARIO_DEMO_ID), eq(id), any())).thenReturn(buildResponse(id, "Firulais"));

        mockMvc.perform(put("/api/mascotas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Firulais"));
    }

    @Test
    void putDevuelve403SiLaMascotaNoEsDelPropietario() throws Exception {
        UUID id = UUID.randomUUID();
        when(petService.update(eq(PROPIETARIO_DEMO_ID), eq(id), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta mascota"));

        mockMvc.perform(put("/api/mascotas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void putDevuelve404SiLaMascotaNoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(petService.update(eq(PROPIETARIO_DEMO_ID), eq(id), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        mockMvc.perform(put("/api/mascotas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }
}
