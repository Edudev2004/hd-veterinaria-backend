package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.DTOs.response.DiagnosisResponse;
import com.vethd.backend.mapper.DiagnosisMapper;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Diagnosis;
import com.vethd.backend.repository.AppointmentRepository;
import com.vethd.backend.repository.DiagnosisRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class DiagnosisService {
    private final AppointmentRepository appointmentRepository;
    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisService(AppointmentRepository appointmentRepository,
                             DiagnosisRepository diagnosisRepository) {
        this.appointmentRepository = appointmentRepository;
        this.diagnosisRepository = diagnosisRepository;
    }

    public DiagnosisResponse registrarDiagnostico(UUID appointmentId, UUID veterinarianId, DiagnosisRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (!appointment.getVeterinarian().getId().equals(veterinarianId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el veterinario asignado a esta cita");
        }

        if (!"ATENDIDA".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede registrar diagnostico en citas ATENDIDAS");
        }

        if (diagnosisRepository.existsByAppointment_Id(appointmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un diagnostico registrado para esta cita");
        }

        Diagnosis diagnosis = DiagnosisMapper.toEntity(request, appointment);
        Diagnosis guardado = diagnosisRepository.save(diagnosis);
        return DiagnosisMapper.toResponse(guardado);
    }
}