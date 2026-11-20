package com.vethd.backend.DTOs.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoReporteDTO {
    private String nombreArchivo;
    private byte[] contenido;
}
