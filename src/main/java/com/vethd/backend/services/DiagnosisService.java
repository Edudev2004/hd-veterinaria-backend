package com.vethd.backend.services;

import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {
    private final AppointmentRepository appointmentRepository;
    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisService(AppointmentRepository appointmentRepository,
                             DiagnosisRepository diagnosisRepository) {
        this.appointmentRepository = appointmentRepository;
        this.diagnosisRepository = diagnosisRepository;
    }
}