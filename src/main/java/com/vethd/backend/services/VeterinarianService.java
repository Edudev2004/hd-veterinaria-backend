package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.AvailabilitySlotResponse;
import com.vethd.backend.DTOs.response.VeterinarianResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VeterinarianService {

    List<VeterinarianResponse> listActive(UUID specialtyId);

    List<AvailabilitySlotResponse> getAvailability(UUID veterinarianId, LocalDate date);
}
