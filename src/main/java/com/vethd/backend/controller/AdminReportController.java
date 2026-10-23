package com.vethd.backend.controller;

import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import com.vethd.backend.DTOs.response.report.DashboardEstadisticasResponse;
import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;
import com.vethd.backend.model.User;
import com.vethd.backend.repository.UserRepository;
import com.vethd.backend.services.AdminReportService;
import com.vethd.backend.services.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    private void validarRolAdmin() {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        boolean esAdmin = user.getRole() != null &&
                user.getRole().getName() != null &&
                (user.getRole().getName().equalsIgnoreCase("ADMIN") ||
                 user.getRole().getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: se requiere rol ADMIN");
        }
    }

    @GetMapping("/reportes/citas")
    public ResponseEntity<CitasReporteResponse> getReporteCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        validarRolAdmin();
        return ResponseEntity.ok(adminReportService.obtenerReporteCitas(fechaInicio, fechaFin));
    }

    @GetMapping("/reportes/veterinarios")
    public ResponseEntity<List<VeterinarioRankingResponse>> getRankingVeterinarios() {
        validarRolAdmin();
        return ResponseEntity.ok(adminReportService.obtenerRankingVeterinarios());
    }

    @GetMapping("/reportes/mascotas")
    public ResponseEntity<List<MascotasEspecieResponse>> getReporteMascotas() {
        validarRolAdmin();
        return ResponseEntity.ok(adminReportService.obtenerReporteMascotasPorEspecie());
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<DashboardEstadisticasResponse> getEstadisticasDashboard() {
        validarRolAdmin();
        return ResponseEntity.ok(adminReportService.obtenerEstadisticasDashboard());
    }
}