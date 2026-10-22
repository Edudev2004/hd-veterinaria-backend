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
import com.vethd.backend.services.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public CitasReporteResponse obtenerReporteCitas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime start = fechaInicio.atStartOfDay();
        LocalDateTime end = fechaFin.atTime(LocalTime.MAX);

        long total = appointmentRepository.countAppointmentsBetween(start, end);
        List<EstadoCitaCount> counts = appointmentRepository.countAppointmentsByStatusBetween(start, end);

        Map<String, Long> desglose = new HashMap<>();
        for (EstadoCitaCount c : counts) {
            desglose.put(c.getStatus(), c.getCount());
        }

        return CitasReporteResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalCitas(total)
                .desglosePorEstado(desglose)
                .build();
    }

    @Override
    public List<VeterinarioRankingResponse> obtenerRankingVeterinarios() {
        List<VeterinarioCitasCount> ranking = appointmentRepository.findRankingVeterinariosAtendidos();
        return ranking.stream()
                .map(r -> VeterinarioRankingResponse.builder()
                        .veterinarianId(r.getVeterinarianId())
                        .nombreCompleto(r.getFullName())
                        .citasAtendidas(r.getCount() != null ? r.getCount() : 0L)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<MascotasEspecieResponse> obtenerReporteMascotasPorEspecie() {
        List<EspecieCount> counts = petRepository.countPetsBySpecies();
        return counts.stream()
                .map(c -> MascotasEspecieResponse.builder()
                        .especie(c.getSpecies())
                        .cantidad(c.getCount() != null ? c.getCount() : 0L)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DashboardEstadisticasResponse obtenerEstadisticasDashboard() {
        long totalClientes = ownerRepository.count();
        long totalVetsActivos = veterinarianRepository.countByActiveTrue();
        long totalMascotas = petRepository.count();
        long totalCitas = appointmentRepository.count();

        return DashboardEstadisticasResponse.builder()
                .totalClientes(totalClientes)
                .totalVeterinariosActivos(totalVetsActivos)
                .totalMascotas(totalMascotas)
                .totalCitas(totalCitas)
                .build();
    }
}