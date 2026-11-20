package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import com.vethd.backend.DTOs.response.report.DashboardEstadisticasResponse;
import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.PetRepository;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.repository.projection.EspecieCount;
import com.vethd.backend.repository.projection.EstadoCitaCount;
import com.vethd.backend.repository.projection.VeterinarioCitasCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private VeterinarianRepository veterinarianRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    private EstadoCitaCount estado(String status, long count) {
        return new EstadoCitaCount() {
            public String getStatus() { return status; }
            public Long getCount() { return count; }
        };
    }

    private EspecieCount especie(String species, Long count) {
        return new EspecieCount() {
            public String getSpecies() { return species; }
            public Long getCount() { return count; }
        };
    }

    private VeterinarioCitasCount ranking(UUID id, String fullName, Long count) {
        return new VeterinarioCitasCount() {
            public UUID getVeterinarianId() { return id; }
            public String getFullName() { return fullName; }
            public Long getCount() { return count; }
        };
    }

    @Test
    void obtenerReporteCitas_usaElDiaCompletoYAgrupaPorEstado() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(LocalTime.MAX);

        when(appointmentRepository.countAppointmentsBetween(start, end)).thenReturn(5L);
        when(appointmentRepository.countAppointmentsByStatusBetween(start, end))
                .thenReturn(List.of(estado("ATENDIDA", 3), estado("CANCELADA", 2)));

        CitasReporteResponse response = adminReportService.obtenerReporteCitas(inicio, fin);

        assertThat(response.getFechaInicio()).isEqualTo(inicio);
        assertThat(response.getFechaFin()).isEqualTo(fin);
        assertThat(response.getTotalCitas()).isEqualTo(5L);
        assertThat(response.getDesglosePorEstado())
                .containsEntry("ATENDIDA", 3L)
                .containsEntry("CANCELADA", 2L)
                .hasSize(2);
    }

    @Test
    void obtenerReporteCitas_sinCitasDevuelveDesgloseVacio() {
        LocalDate dia = LocalDate.of(2026, 11, 19);
        LocalDateTime start = dia.atStartOfDay();
        LocalDateTime end = dia.atTime(LocalTime.MAX);

        when(appointmentRepository.countAppointmentsBetween(start, end)).thenReturn(0L);
        when(appointmentRepository.countAppointmentsByStatusBetween(start, end)).thenReturn(List.of());

        CitasReporteResponse response = adminReportService.obtenerReporteCitas(dia, dia);

        assertThat(response.getTotalCitas()).isZero();
        assertThat(response.getDesglosePorEstado()).isEmpty();
    }

    @Test
    void obtenerRankingVeterinarios_mapeaNombreYConteoNuloComoCero() {
        UUID vet1 = UUID.randomUUID();
        UUID vet2 = UUID.randomUUID();
        when(appointmentRepository.findRankingVeterinariosAtendidos())
                .thenReturn(List.of(ranking(vet1, "Ana Torres", 7L), ranking(vet2, "Juan Pérez", null)));

        List<VeterinarioRankingResponse> response = adminReportService.obtenerRankingVeterinarios();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getVeterinarianId()).isEqualTo(vet1);
        assertThat(response.get(0).getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(response.get(0).getCitasAtendidas()).isEqualTo(7L);
        assertThat(response.get(1).getCitasAtendidas()).isZero();
    }

    @Test
    void obtenerReporteMascotasPorEspecie_mapeaEspecieYCantidad() {
        when(petRepository.countPetsBySpecies())
                .thenReturn(List.of(especie("perro", 4L), especie("gato", null)));

        List<MascotasEspecieResponse> response = adminReportService.obtenerReporteMascotasPorEspecie();

        assertThat(response).extracting(MascotasEspecieResponse::getEspecie).containsExactly("perro", "gato");
        assertThat(response).extracting(MascotasEspecieResponse::getCantidad).containsExactly(4L, 0L);
    }

    @Test
    void obtenerEstadisticasDashboard_cuentaSoloMascotasActivas() {
        when(ownerRepository.count()).thenReturn(10L);
        when(veterinarianRepository.countByActiveTrue()).thenReturn(3L);
        when(petRepository.countByActiveTrue()).thenReturn(12L);
        when(appointmentRepository.count()).thenReturn(25L);

        DashboardEstadisticasResponse response = adminReportService.obtenerEstadisticasDashboard();

        assertThat(response.getTotalClientes()).isEqualTo(10L);
        assertThat(response.getTotalVeterinariosActivos()).isEqualTo(3L);
        assertThat(response.getTotalMascotas()).isEqualTo(12L);
        assertThat(response.getTotalCitas()).isEqualTo(25L);
        verify(petRepository, never()).count();
    }
}
