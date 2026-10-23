package com.vethd.backend.services;

import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import com.vethd.backend.DTOs.response.report.DashboardEstadisticasResponse;
import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;

import java.time.LocalDate;
import java.util.List;

public interface AdminReportService {
    CitasReporteResponse obtenerReporteCitas(LocalDate fechaInicio, LocalDate fechaFin);
    List<VeterinarioRankingResponse> obtenerRankingVeterinarios();
    List<MascotasEspecieResponse> obtenerReporteMascotasPorEspecie();
    DashboardEstadisticasResponse obtenerEstadisticasDashboard();
}