package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEstadisticasResponse {
    private long totalClientes;
    private long totalVeterinariosActivos;
    private long totalMascotas;
    private long totalCitas;
}