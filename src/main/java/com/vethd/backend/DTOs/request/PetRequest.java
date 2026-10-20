package com.vethd.backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PetRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Species is required")
        String species,
        String breed,
        @NotBlank(message = "Gender is required")
        String gender,
        LocalDate birthDate,
        String observations,
        String photoUrl
) {
}
