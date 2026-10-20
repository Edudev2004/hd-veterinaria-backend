package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.response.VeterinarianResponse;
import com.vethd.backend.model.Specialty;
import com.vethd.backend.model.User;
import com.vethd.backend.model.Veterinarian;

import java.util.stream.Collectors;

public class VeterinarianMapper {

    private VeterinarianMapper() {

    }

    public static Veterinarian toEntity(User user) {
        Veterinarian veterinarian = new Veterinarian();
        veterinarian.setUser(user);
        return veterinarian;
    }

    public static VeterinarianResponse toResponse(Veterinarian veterinarian) {
        User user = veterinarian.getUser();
        String specialtyNames = veterinarian.getSpecialties().stream()
                .map(Specialty::getName)
                .collect(Collectors.joining(", "));

        return VeterinarianResponse.builder()
                .id(veterinarian.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .specialtyName(specialtyNames)
                .email(user.getEmail())
                .build();
    }
}
