package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);
}
