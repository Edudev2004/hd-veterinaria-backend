package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.DTOs.response.VetPatientResponse;
import com.vethd.backend.mapper.OwnerMapper;
import com.vethd.backend.mapper.PetMapper;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Pet;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.services.VetPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VetPatientServiceImpl implements VetPatientService {

    private static final String STATUS_ATENDIDA = "ATENDIDA";
    private static final String STATUS_CANCELADA = "CANCELADA";

    private final AppointmentRepository appointmentRepository;

    // Pacientes = mascotas con las que el veterinario tiene (o tuvo) citas no canceladas
    @Override
    @Transactional(readOnly = true)
    public List<VetPatientResponse> getPacientes(UUID veterinarianUserId) {
        List<Appointment> appointments = appointmentRepository
                .findByVeterinarianUserIdOrderByScheduledAtDesc(veterinarianUserId);

        Map<UUID, List<Appointment>> citasPorMascota = appointments.stream()
                .filter(a -> !STATUS_CANCELADA.equals(a.getStatus()))
                .collect(Collectors.groupingBy(
                        a -> a.getPet().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return citasPorMascota.values().stream()
                .map(this::toPatientResponse)
                .sorted(Comparator
                        .comparing(VetPatientResponse::ultimaAtencion,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(p -> p.pet().name(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private VetPatientResponse toPatientResponse(List<Appointment> citas) {
        Pet pet = citas.get(0).getPet();

        List<Appointment> atendidas = citas.stream()
                .filter(a -> STATUS_ATENDIDA.equals(a.getStatus()))
                .toList();

        LocalDateTime ultimaAtencion = atendidas.stream()
                .map(Appointment::getScheduledAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        OwnerResponse owner = (pet.getOwner() != null && pet.getOwner().getUser() != null)
                ? OwnerMapper.toResponse(pet.getOwner())
                : null;

        return VetPatientResponse.builder()
                .pet(PetMapper.toResponse(pet))
                .owner(owner)
                .totalAtenciones(atendidas.size())
                .ultimaAtencion(ultimaAtencion)
                .build();
    }
}