package com.vethd.backend.services;

import com.vethd.backend.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

@Service
public class VetScheduleService {
    private final AppointmentRepository appointmentRepository;

    public VetScheduleService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
}