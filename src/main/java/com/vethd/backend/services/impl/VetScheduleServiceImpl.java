package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.mapper.AppointmentMapper;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.services.VetScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VetScheduleServiceImpl implements VetScheduleService {

    private final AppointmentRepository appointmentRepository;

    @Override
    public List<AppointmentResponse> getAgendaDiaria(UUID veterinarianId, LocalDate fecha) {
        LocalDateTime desde = LocalDateTime.of(fecha, LocalTime.MIN);
        LocalDateTime hasta = LocalDateTime.of(fecha, LocalTime.MAX);
        return appointmentRepository
                .findByVeterinarian_IdAndScheduledAtBetweenOrderByScheduledAtAsc(veterinarianId, desde, hasta)
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }
}
