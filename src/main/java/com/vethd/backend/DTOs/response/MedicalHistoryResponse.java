package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MedicalHistoryResponse(
        PetResponse pet,
        Integer totalEntries,
        Integer totalAtenciones,
        LocalDateTime lastAttentionDate,
        LocalDateTime ultimaAtencion,
        LocalDateTime firstAttentionDate,
        LocalDateTime primeraAtencion,
        List<MedicalHistoryEntryResponse> entries,
        List<MedicalHistoryEntryResponse> entradas
) {
}

