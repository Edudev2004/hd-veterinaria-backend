package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.AppointmentNotAttendedRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;

import java.util.UUID;

public interface AppointmentStatusService {

    AppointmentResponse marcarAtendida(UUID appointmentId, UUID veterinarianId);

    AppointmentResponse marcarNoAtendida(UUID appointmentId, UUID veterinarianId, AppointmentNotAttendedRequest request);
}
