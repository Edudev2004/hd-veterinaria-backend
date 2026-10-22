package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.MedicalHistoryEntryResponse;
import com.vethd.backend.DTOs.response.MedicalHistoryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PetHistoryService {
    MedicalHistoryResponse getPetMedicalHistory(UUID petId, String busqueda, UUID veterinarioId, LocalDate fechaDesde, LocalDate fechaHasta);

    List<MedicalHistoryEntryResponse> getPetMedicalHistoryEntries(UUID petId, String busqueda, UUID veterinarioId, LocalDate fechaDesde, LocalDate fechaHasta);
}

