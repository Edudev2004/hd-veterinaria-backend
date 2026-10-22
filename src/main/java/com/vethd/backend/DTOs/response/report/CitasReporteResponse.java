package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitasReporteResponse {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long totalCitas;
    private Map<String, Long> desglosePorEstado;
}