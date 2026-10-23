package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioRankingResponse {
    private UUID veterinarianId;
    private String nombreCompleto;
    private long citasAtendidas;
}