package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.response.MedicalHistoryEntryResponse;
import com.vethd.backend.DTOs.response.MedicalHistoryResponse;
import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Diagnosis;
import com.vethd.backend.model.Pet;

import java.time.LocalDateTime;
import java.util.List;

public class MedicalHistoryMapper {

    private MedicalHistoryMapper() {
    }

    public static MedicalHistoryEntryResponse toEntryResponse(Diagnosis diagnosis) {
        Appointment appointment = diagnosis.getAppointment();
        return MedicalHistoryEntryResponse.builder()
                .id(diagnosis.getId())
                .appointmentId(appointment.getId())
                .petId(appointment.getPet().getId())
                .attentionDate(appointment.getScheduledAt())
                .veterinarian(VeterinarianMapper.toResponse(appointment.getVeterinarian()))
                .consultationReason(appointment.getReason())
                .diagnosis(diagnosis.getDiagnosis())
                .details(diagnosis.getDetails())
                .treatment(diagnosis.getTreatment())
                .notes(diagnosis.getNotes())
                .nextSuggestedAppointment(diagnosis.getNextSuggestedAppointment())
                .createdAt(diagnosis.getCreatedAt())
                .build();
    }

    public static MedicalHistoryResponse toResponse(Pet pet, List<MedicalHistoryEntryResponse> entries) {
        LocalDateTime latest = (entries != null && !entries.isEmpty()) ? entries.get(0).attentionDate() : null;
        LocalDateTime earliest = (entries != null && !entries.isEmpty()) ? entries.get(entries.size() - 1).attentionDate() : null;
        int size = entries != null ? entries.size() : 0;
        List<MedicalHistoryEntryResponse> safeEntries = entries != null ? entries : List.of();

        PetResponse petResponse = PetMapper.toResponse(pet);

        return MedicalHistoryResponse.builder()
                .pet(petResponse)
                .totalEntries(size)
                .totalAtenciones(size)
                .lastAttentionDate(latest)
                .ultimaAtencion(latest)
                .firstAttentionDate(earliest)
                .primeraAtencion(earliest)
                .entries(safeEntries)
                .entradas(safeEntries)
                .build();
    }
}

