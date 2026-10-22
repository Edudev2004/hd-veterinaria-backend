package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;

import java.util.List;
import java.util.UUID;

public interface PetService {

    PetResponse register(UUID ownerId, PetRequest request);

    List<PetResponse> listByOwner(UUID ownerId);

    PetResponse update(UUID ownerId, UUID petId, PetRequest request);
}
