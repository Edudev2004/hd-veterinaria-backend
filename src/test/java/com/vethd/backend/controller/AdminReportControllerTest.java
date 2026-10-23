package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import com.vethd.backend.DTOs.response.report.DashboardEstadisticasResponse;
import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;
import com.vethd.backend.model.Role;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.AdminReportService;
import com.vethd.backend.services.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminReportService adminReportService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminReportController adminReportController;

    private UUID adminUserId;
    private User adminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController).build();

        adminUserId = UUID.randomUUID();
        Role roleAdmin = Role.builder().name("ADMIN").build();
        adminUser = User.builder()
                .id(adminUserId)
                .email("admin@vethd.com")
                .role(roleAdmin)
                .build();
    }

    private void simularSesionAdmin() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(adminUserId);
        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("GET /api/admin/reportes/citas con ADMIN debe retornar 200 y desglose")
    void testGetReporteCitas_Admin() throws Exception {
        simularSesionAdmin();

        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fin = LocalDate.of(2026, 1, 31);
        CitasReporteResponse response = CitasReporteResponse.builder()
                .fechaInicio(inicio)
                .fechaFin(fin)
                .totalCitas(5L)
                .desglosePorEstado(Map.of("COMPLETADA", 5L))
                .build();

        when(adminReportService.obtenerReporteCitas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/admin/reportes/citas")
                        .param("fechaInicio", "2026-01-01")
                        .param("fechaFin", "2026-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCitas").value(5))
                .andExpect(jsonPath("$.desglosePorEstado.COMPLETADA").value(5));
    }

    @Test
    @DisplayName("GET /api/admin/reportes/veterinarios con ADMIN debe retornar ranking 200")
    void testGetRankingVeterinarios_Admin() throws Exception {
        simularSesionAdmin();

        VeterinarioRankingResponse vet = VeterinarioRankingResponse.builder()
                .veterinarianId(UUID.randomUUID())
                .nombreCompleto("Dra. Maria Lopez")
                .citasAtendidas(12L)
                .build();

        when(adminReportService.obtenerRankingVeterinarios()).thenReturn(List.of(vet));

        mockMvc.perform(get("/api/admin/reportes/veterinarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCompleto").value("Dra. Maria Lopez"))
                .andExpect(jsonPath("$[0].citasAtendidas").value(12));
    }

    @Test
    @DisplayName("GET /api/admin/reportes/mascotas con ADMIN debe retornar 200")
    void testGetReporteMascotas_Admin() throws Exception {
        simularSesionAdmin();

        MascotasEspecieResponse especie = MascotasEspecieResponse.builder()
                .especie("Felino")
                .cantidad(18L)
                .build();

        when(adminReportService.obtenerReporteMascotasPorEspecie()).thenReturn(List.of(especie));

        mockMvc.perform(get("/api/admin/reportes/mascotas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especie").value("Felino"))
                .andExpect(jsonPath("$[0].cantidad").value(18));
    }

    @Test
    @DisplayName("GET /api/admin/estadisticas con ADMIN debe retornar 200")
    void testGetEstadisticasDashboard_Admin() throws Exception {
        simularSesionAdmin();

        DashboardEstadisticasResponse stats = DashboardEstadisticasResponse.builder()
                .totalClientes(40L)
                .totalVeterinariosActivos(3L)
                .totalMascotas(60L)
                .totalCitas(100L)
                .build();

        when(adminReportService.obtenerEstadisticasDashboard()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/estadisticas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes").value(40))
                .andExpect(jsonPath("$.totalVeterinariosActivos").value(3))
                .andExpect(jsonPath("$.totalMascotas").value(60))
                .andExpect(jsonPath("$.totalCitas").value(100));
    }

    @Test
    @DisplayName("Cualquier endpoint sin rol ADMIN debe retornar 403 Forbidden")
    void testAccesoDenegadoParaNoAdmin() throws Exception {
        UUID clienteId = UUID.randomUUID();
        Role roleClient = Role.builder().name("CLIENT").build();
        User clientUser = User.builder()
                .id(clienteId)
                .email("cliente@vethd.com")
                .role(roleClient)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(clienteId);
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(clientUser));

        mockMvc.perform(get("/api/admin/estadisticas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}