package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentUpdateRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.model.*;

public class AppointmentMapper {

    private AppointmentMapper() {

    }

    public static Appointment toEntity(AppointmentRequest request, Pet pet,
                                       Veterinarian veterinarian, Specialty specialty) {
        Appointment appointment = new Appointment();
        appointment.setPet(pet);
        appointment.setVeterinarian(veterinarian);
        appointment.setSpecialty(specialty);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setReason(request.reason());
        return appointment;
    }

    public static void updateEntity(AppointmentUpdateRequest request, Appointment appointment,
                                    Veterinarian veterinarian) {
        appointment.setVeterinarian(veterinarian);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setReason(request.reason());
    }

    public static AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .pet(PetMapper.toResponse(appointment.getPet()))
                .veterinarian(VeterinarianMapper.toResponse(appointment.getVeterinarian()))
                .scheduledAt(appointment.getScheduledAt())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
