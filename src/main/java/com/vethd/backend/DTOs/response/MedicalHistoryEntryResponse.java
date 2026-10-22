package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MedicalHistoryEntryResponse(
        UUID id,
        UUID appointmentId,
        UUID petId,
        LocalDateTime attentionDate,
        VeterinarianResponse veterinarian,
        String consultationReason,
        String diagnosis,
        String details,
        String treatment,
        String notes,
        LocalDate nextSuggestedAppointment,
        LocalDateTime createdAt
) {
}

