package com.vethd.backend.services;

import com.vethd.backend.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

@Service
public class AppointmentStatusService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentStatusService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
}