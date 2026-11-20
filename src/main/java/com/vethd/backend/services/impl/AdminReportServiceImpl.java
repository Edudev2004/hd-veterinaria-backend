package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.report.CitaReporteDetalleDTO;
import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import com.vethd.backend.DTOs.response.report.DashboardEstadisticasResponse;
import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Veterinarian;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        List<Appointment> appointments = appointmentRepository.findWithDetailsBetweenOrderByScheduledAtDesc(start, end);
        List<CitaReporteDetalleDTO> detalleList = (appointments != null) ? appointments.stream()
                .map(a -> {
                    String propNombre = (a.getPet() != null && a.getPet().getOwner() != null && a.getPet().getOwner().getUser() != null)
                            ? (a.getPet().getOwner().getUser().getFirstName() + " " + a.getPet().getOwner().getUser().getLastName()).trim()
                            : "N/A";
                    String propTel = (a.getPet() != null && a.getPet().getOwner() != null && a.getPet().getOwner().getPhone() != null)
                            ? a.getPet().getOwner().getPhone()
                            : "";
                    String propEmail = (a.getPet() != null && a.getPet().getOwner() != null && a.getPet().getOwner().getUser() != null)
                            ? (a.getPet().getOwner().getUser().getEmail() != null ? a.getPet().getOwner().getUser().getEmail() : "")
                            : "";
                    String vetNombre = (a.getVeterinarian() != null && a.getVeterinarian().getUser() != null)
                            ? (a.getVeterinarian().getUser().getFirstName() + " " + a.getVeterinarian().getUser().getLastName()).trim()
                            : "N/A";
                    String esp = (a.getSpecialty() != null) ? a.getSpecialty().getName() : "General";
                    String obs = (a.getCancellationReason() != null && !a.getCancellationReason().isBlank())
                            ? a.getCancellationReason()
                            : ("ATENDIDA".equalsIgnoreCase(a.getStatus()) ? "Atención médica completada" : "-");

                    return CitaReporteDetalleDTO.builder()
                            .id(a.getId())
                            .fechaHora(a.getScheduledAt())
                            .estado(a.getStatus())
                            .mascotaNombre(a.getPet() != null ? a.getPet().getName() : "N/A")
                            .especie(a.getPet() != null ? a.getPet().getSpecies() : "N/A")
                            .raza(a.getPet() != null && a.getPet().getBreed() != null ? a.getPet().getBreed() : "Mestizo")
                            .propietarioNombre(propNombre)
                            .propietarioTelefono(propTel)
                            .propietarioEmail(propEmail)
                            .veterinarioNombre(vetNombre)
                            .especialidad(esp)
                            .motivo(a.getReason() != null ? a.getReason() : "Consulta médica")
                            .observacion(obs)
                            .build();
                })
                .collect(Collectors.toList()) : new ArrayList<>();

        return CitasReporteResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalCitas(total)
                .desglosePorEstado(desglose)
                .detalleCitas(detalleList)
                .build();
    }

    @Override
    public List<VeterinarioRankingResponse> obtenerRankingVeterinarios() {
        List<VeterinarioCitasCount> ranking = appointmentRepository.findRankingVeterinariosAtendidos();
        Map<UUID, Long> conteos = (ranking != null) ? ranking.stream()
                .filter(r -> r.getVeterinarianId() != null)
                .collect(Collectors.toMap(
                        VeterinarioCitasCount::getVeterinarianId,
                        r -> r.getCount() != null ? r.getCount() : 0L,
                        (v1, v2) -> v1
                )) : new HashMap<>();

        List<Veterinarian> todosVets = veterinarianRepository.findAll();
        if (todosVets.isEmpty() && ranking != null) {
            return ranking.stream()
                    .map(r -> VeterinarioRankingResponse.builder()
                            .veterinarianId(r.getVeterinarianId())
                            .nombreCompleto(r.getFullName())
                            .citasAtendidas(r.getCount() != null ? r.getCount() : 0L)
                            .build())
                    .collect(Collectors.toList());
        }

        return todosVets.stream()
                .map(v -> {
                    String nombre = (v.getUser() != null)
                            ? (v.getUser().getFirstName() + " " + v.getUser().getLastName()).trim()
                            : "Veterinario";
                    String email = (v.getUser() != null && v.getUser().getEmail() != null) ? v.getUser().getEmail() : "";
                    String especialidad = (v.getSpecialties() != null && !v.getSpecialties().isEmpty())
                            ? v.getSpecialties().stream().map(com.vethd.backend.model.Specialty::getName).collect(Collectors.joining(", "))
                            : "Medicina General";
                    long atenciones = conteos.getOrDefault(v.getId(), 0L);

                    return VeterinarioRankingResponse.builder()
                            .veterinarianId(v.getId())
                            .nombreCompleto(nombre)
                            .email(email)
                            .especialidad(especialidad)
                            .activo(v.isActive())
                            .citasAtendidas(atenciones)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getCitasAtendidas(), a.getCitasAtendidas()))
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
        long totalMascotas = petRepository.countByActiveTrue();
        long totalCitas = appointmentRepository.count();

        return DashboardEstadisticasResponse.builder()
                .totalClientes(totalClientes)
                .totalVeterinariosActivos(totalVetsActivos)
                .totalMascotas(totalMascotas)
                .totalCitas(totalCitas)
                .build();
    }
}