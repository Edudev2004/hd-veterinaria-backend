package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.VetPatientResponse;

import java.util.List;
import java.util.UUID;

public interface VetPatientService {

    List<VetPatientResponse> getPacientes(UUID veterinarianUserId);
}