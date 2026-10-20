package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.SpecialtyRequest;
import com.vethd.backend.DTOs.response.SpecialtyResponse;
import com.vethd.backend.model.Specialty;

public class SpecialtyMapper {

    private SpecialtyMapper() {

    }

    public static Specialty toEntity(SpecialtyRequest request) {
        Specialty specialty = new Specialty();
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setActive(request.active());
        return specialty;
    }

    public static void updateEntity(SpecialtyRequest request, Specialty specialty) {
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setActive(request.active());
    }

    public static SpecialtyResponse toResponse(Specialty specialty) {
        return SpecialtyResponse.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .active(specialty.isActive())
                .build();
    }
}
