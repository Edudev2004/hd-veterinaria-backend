package com.vethd.backend.services.impl;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.mapper.PetMapper;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Pet;
import com.vethd.backend.repository.OwnerRepository;
import com.vethd.backend.repository.PetRepository;
import com.vethd.backend.services.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    @Override
    @Transactional
    public PetResponse register(UUID ownerId, PetRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propietario no encontrado"));

        Pet saved = petRepository.save(PetMapper.toEntity(request, owner));
        return PetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> listByOwner(UUID ownerId) {
        return petRepository.findByOwner_IdAndActiveTrueOrderByCreatedAtDesc(ownerId).stream()
                .map(PetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PetResponse update(UUID ownerId, UUID petId, PetRequest request) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta mascota");
        }

        PetMapper.updateEntity(request, pet);
        return PetMapper.toResponse(petRepository.save(pet));
    }
}
