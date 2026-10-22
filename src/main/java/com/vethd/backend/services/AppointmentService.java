package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.AppointmentCancelRequest;
import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;

import java.util.UUID;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse rescheduleAppointment(UUID id, AppointmentRescheduledRequest request);

    AppointmentResponse cancelAppointment(UUID id, AppointmentCancelRequest request);
}
