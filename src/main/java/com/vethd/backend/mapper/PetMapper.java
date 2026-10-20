package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.PetRequest;
import com.vethd.backend.DTOs.response.PetResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.Pet;

public class PetMapper {

    private PetMapper() {

    }

    public static Pet toEntity(PetRequest request, Owner owner) {
        Pet pet = new Pet();
        pet.setOwner(owner);
        pet.setName(request.name());
        pet.setSpecies(request.species());
        pet.setBreed(request.breed());
        pet.setGender(request.gender());
        pet.setBirthDate(request.birthDate());
        pet.setObservations(request.observations());
        pet.setPhotoUrl(request.photoUrl());
        return pet;
    }

    public static void updateEntity(PetRequest request, Pet pet) {
        pet.setName(request.name());
        pet.setSpecies(request.species());
        pet.setBreed(request.breed());
        pet.setGender(request.gender());
        pet.setBirthDate(request.birthDate());
        pet.setObservations(request.observations());
        pet.setPhotoUrl(request.photoUrl());
    }

    public static PetResponse toResponse(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .gender(pet.getGender())
                .birthDate(pet.getBirthDate())
                .observations(pet.getObservations())
                .photoUrl(pet.getPhotoUrl())
                .active(pet.isActive())
                .createdAt(pet.getCreatedAt())
                .build();
    }
}
