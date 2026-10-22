package com.vethd.backend.services;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;

import java.util.UUID;

public interface OwnerService {

    OwnerResponse getProfile(UUID ownerId);

    OwnerResponse updateProfile(UUID ownerId, OwnerRequest request);
}
