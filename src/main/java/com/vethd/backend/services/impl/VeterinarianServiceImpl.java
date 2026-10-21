package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.AvailabilitySlotResponse;
import com.vethd.backend.DTOs.response.VeterinarianResponse;
import com.vethd.backend.mapper.VeterinarianMapper;
import com.vethd.backend.model.Veterinarian;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.VeterinarianRepository;
import com.vethd.backend.services.VeterinarianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinarianServiceImpl implements VeterinarianService {

    private static final LocalTime START_HOUR = LocalTime.of(8, 0);
    private static final int SLOT_COUNT = 10;

    private final VeterinarianRepository veterinarianRepository;
    private final AppointmentRepository appointmentRepository;


    @Override
    public List<VeterinarianResponse> listActive(UUID specialtyId) {
        List<Veterinarian> veterinarians = (specialtyId == null)
                ? veterinarianRepository.findActiveAll()
                : veterinarianRepository.findActiveBySpecialty(specialtyId);

        Map<UUID, Double> ratings = veterinarianRepository.findAverageRatings().stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Double) row[1]
                ));

        return veterinarians.stream()
                .map(vet -> VeterinarianMapper.toResponse(
                        vet, round(ratings.get(vet.getId())))
                ).toList();
    }

    @Override
    public List<AvailabilitySlotResponse> getAvailability(UUID veterinarianId, LocalDate date) {

        if (!veterinarianRepository.existsById(veterinarianId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinarian not found");
        }

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(LocalTime.MAX);

        Set<LocalTime> booked = appointmentRepository.findByVeterinarian_IdAndScheduledAtBetweenAndStatus(
                        veterinarianId, from, to, "PENDIENTE"
                ).stream()
                .map(appointment -> appointment.getScheduledAt().toLocalTime())
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            LocalTime hour = START_HOUR.plusHours(i);
            boolean inFuture = date.atTime(hour).isAfter(now);
            slots.add(AvailabilitySlotResponse.builder()
                    .hour(hour)
                    .available(!booked.contains(hour) && inFuture)
                    .build());
        }

        return slots;
    }


    private Double round(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return Math.round(valor * 100.0) / 100.0;
    }
}
