package com.vethd.backend.controller;


import com.vethd.backend.model.Specialty;
import com.vethd.backend.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpecialtyRepository specialtyRepository;



    @BeforeEach
    void setUp(){
        specialtyRepository.deleteAll();

        specialtyRepository.save(Specialty.builder()
                .name("Cardiología")
                .description("Corazón")
                .active(true)
                .build()
        );

        specialtyRepository.save(Specialty.builder()
                .name("Dermatología")
                .description("Piel")
                .active(true)
                .build()
        );

        specialtyRepository.save(Specialty.builder()
                .name("Cirugía")
                .description("Operaciones")
                .active(false)
                .build()
        );
    }

    @Test
    void debeRetornarSoloLasEspecialidadesActivas() throws Exception {
        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", hasItems("Cardiología", "Dermatología")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Cirugía"))));
    }

    @Test
    void debeIncluirIdNombreYDescripcion() throws Exception {
        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].active").exists());
    }
}
