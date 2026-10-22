package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.OwnerRequest;
import com.vethd.backend.DTOs.response.OwnerResponse;
import com.vethd.backend.model.Owner;
import com.vethd.backend.model.User;

public class OwnerMapper {

    private OwnerMapper() {

    }

    public static Owner toEntity(OwnerRequest request, User user) {
        Owner owner = new Owner();
        owner.setUser(user);
        owner.setPhone(request.phone());
        owner.setAddress(request.address());
        return owner;
    }

    // Un campo null significa "sin cambios"; photoUrl en blanco borra la foto.
    public static void updateEntity(OwnerRequest request, Owner owner) {
        User user = owner.getUser();

        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.photoUrl() != null) {
            user.setPhotoUrl(request.photoUrl().isBlank() ? null : request.photoUrl().trim());
        }
        if (request.phone() != null) {
            owner.setPhone(request.phone());
        }
        if (request.address() != null) {
            owner.setAddress(request.address());
        }
    }

    public static OwnerResponse toResponse(Owner owner) {
        User user = owner.getUser();
        return OwnerResponse.builder()
                .id(owner.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(owner.getPhone())
                .address(owner.getAddress())
                .photoUrl(user.getPhotoUrl())
                .build();
    }
}
