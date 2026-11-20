package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaReporteDetalleDTO {
    private UUID id;
    private LocalDateTime fechaHora;
    private String estado;
    private String mascotaNombre;
    private String especie;
    private String raza;
    private String propietarioNombre;
    private String propietarioTelefono;
    private String propietarioEmail;
    private String veterinarioNombre;
    private String especialidad;
    private String motivo;
    private String observacion;
}
