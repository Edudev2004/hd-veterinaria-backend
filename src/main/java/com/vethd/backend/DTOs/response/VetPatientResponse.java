package com.vethd.backend.DTOs.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VetPatientResponse(
        PetResponse pet,
        OwnerResponse owner,
        Integer totalAtenciones,
        LocalDateTime ultimaAtencion
) {
}