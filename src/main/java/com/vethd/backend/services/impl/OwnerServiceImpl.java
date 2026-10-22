package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.mapper.OwnerMapper;
import com.vethd.backend.model.Owner;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.services.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;

    @Override
    @Transactional(readOnly = true)
    public OwnerResponse getProfile(UUID ownerId) {
        return OwnerMapper.toResponse(findOwner(ownerId));
    }

    @Override
    @Transactional
    public OwnerResponse updateProfile(UUID ownerId, OwnerRequest request) {
        Owner owner = findOwner(ownerId);
        OwnerMapper.updateEntity(request, owner);
        return OwnerMapper.toResponse(ownerRepository.save(owner));
    }

    private Owner findOwner(UUID ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propietario no encontrado"));
    }
}
