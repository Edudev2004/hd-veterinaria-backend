package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MascotasEspecieResponse {
    private String especie;
    private long cantidad;
}