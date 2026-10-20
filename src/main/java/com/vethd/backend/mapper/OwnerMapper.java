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

    public static void updateEntity(OwnerRequest request, Owner owner) {
        owner.setPhone(request.phone());
        owner.setAddress(request.address());
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
                .build();
    }
}
