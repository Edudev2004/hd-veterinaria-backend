package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.VeterinarianResponse;

import java.util.List;
import java.util.UUID;

public interface VeterinarianService {

    List<VeterinarianResponse> listActive(UUID specialtyId);
}
