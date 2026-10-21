package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.DTOs.response.DiagnosisResponse;

import java.util.UUID;

public interface DiagnosisService {

    DiagnosisResponse registrarDiagnostico(UUID appointmentId, UUID veterinarianId, DiagnosisRequest request);
}
