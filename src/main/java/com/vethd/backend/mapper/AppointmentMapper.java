package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.AppointmentRequest;
import com.vethd.backend.DTOs.request.AppointmentRescheduledRequest;
import com.vethd.backend.DTOs.response.AppointmentResponse;
import com.vethd.backend.model.*;

import java.time.LocalDateTime;

public class AppointmentMapper {

    private AppointmentMapper() {

    }

    public static Appointment toEntity(AppointmentRequest request, Pet pet,
                                       Veterinarian veterinarian, Specialty specialty) {
        Appointment appointment = new Appointment();
        appointment.setPet(pet);
        appointment.setVeterinarian(veterinarian);
        appointment.setSpecialty(specialty);
        appointment.setScheduledAt(LocalDateTime.of(request.date(), request.hour()));
        appointment.setReason(request.reason());
        appointment.setStatus("PENDIENTE");
        return appointment;
    }

    public static void rescheduleEntity(AppointmentRescheduledRequest request, Appointment appointment, Veterinarian veterinarian) {
        appointment.setVeterinarian(veterinarian);
        appointment.setScheduledAt(LocalDateTime.of(request.newDate(), request.newHour()));
    }


    public static AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .pet(PetMapper.toResponse(appointment.getPet()))
                .veterinarian(VeterinarianMapper.toResponse(appointment.getVeterinarian()))
                .scheduledAt(appointment.getScheduledAt())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
