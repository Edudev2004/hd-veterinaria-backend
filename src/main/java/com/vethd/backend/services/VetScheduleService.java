package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.AppointmentResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VetScheduleService {

    List<AppointmentResponse> getAgendaDiaria(UUID veterinarianId, LocalDate fecha);
}
