package com.vethd.backend.report;

import com.vethd.backend.DTOs.response.report.CitaReporteDetalleDTO;
import com.vethd.backend.DTOs.response.report.CitasReporteResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class CitaExcelGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_SOLO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_SOLO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Color oficial de VetHD: Teal Clínico
    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 13, (byte) 148, (byte) 136}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
    private static final XSSFColor COLOR_FONDO_TOTAL = new XSSFColor(new byte[]{(byte) 240, (byte) 253, (byte) 250}, null);

    private static final String[] COLUMNAS_DETALLE = {
            "N°", "Fecha", "Hora", "Estado", "Mascota", "Especie", "Raza",
            "Propietario", "Teléfono", "Médico Veterinario", "Especialidad", "Motivo de Consulta", "Diagnóstico / Cancelación"
    };

    private static final int[] ANCHOS_DETALLE = {8, 14, 10, 16, 20, 14, 18, 26, 18, 26, 22, 30, 36};
    private static final int FILA_CABECERA = 5;

    private final String razonSocialEmpresa;

    public CitaExcelGenerator(@Value("${app.empresa.razon-social:VetHD - Plataforma Clínica Veterinaria}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(CitasReporteResponse reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);

            // =========================================================================
            // HOJA 1: DETALLE COMPLETO DE CITAS
            // =========================================================================
            Sheet hojaDetalle = libro.createSheet("Citas Médicas");

            // Título
            Row titulo = hojaDetalle.createRow(0);
            crearCelda(titulo, 0, razonSocialEmpresa + " - Reporte Detallado de Citas", estilos.titulo);
            hojaDetalle.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS_DETALLE.length - 1));

            // Metadatos
            crearCelda(hojaDetalle.createRow(1), 0, "Generado: " + LocalDateTime.now().format(FORMATO_FECHA), estilos.info);
            crearCelda(hojaDetalle.createRow(2), 0, "Período: " + reporte.getFechaInicio() + " al " + reporte.getFechaFin(), estilos.info);
            crearCelda(hojaDetalle.createRow(3), 0, "Total de citas registradas: " + reporte.getTotalCitas(), estilos.info);

            // Cabecera
            Row cabecera = hojaDetalle.createRow(FILA_CABECERA);
            for (int i = 0; i < COLUMNAS_DETALLE.length; i++) {
                crearCelda(cabecera, i, COLUMNAS_DETALLE[i], estilos.cabecera);
                hojaDetalle.setColumnWidth(i, ANCHOS_DETALLE[i] * 256);
            }

            int nroFila = FILA_CABECERA + 1;
            List<CitaReporteDetalleDTO> detalleList = reporte.getDetalleCitas();
            if (detalleList != null && !detalleList.isEmpty()) {
                int itemIndex = 1;
                for (CitaReporteDetalleDTO c : detalleList) {
                    Row fila = hojaDetalle.createRow(nroFila++);
                    crearCelda(fila, 0, itemIndex++, estilos.datoCentro);
                    crearCelda(fila, 1, c.getFechaHora() != null ? c.getFechaHora().format(FORMATO_SOLO_FECHA) : "-", estilos.datoCentro);
                    crearCelda(fila, 2, c.getFechaHora() != null ? c.getFechaHora().format(FORMATO_SOLO_HORA) : "-", estilos.datoCentro);
                    crearCelda(fila, 3, c.getEstado() != null ? c.getEstado() : "-", estilos.datoCentro);
                    crearCelda(fila, 4, c.getMascotaNombre(), estilos.datoTexto);
                    crearCelda(fila, 5, c.getEspecie(), estilos.datoCentro);
                    crearCelda(fila, 6, c.getRaza(), estilos.datoTexto);
                    crearCelda(fila, 7, c.getPropietarioNombre(), estilos.datoTexto);
                    crearCelda(fila, 8, c.getPropietarioTelefono(), estilos.datoCentro);
                    crearCelda(fila, 9, c.getVeterinarioNombre(), estilos.datoTexto);
                    crearCelda(fila, 10, c.getEspecialidad(), estilos.datoTexto);
                    crearCelda(fila, 11, c.getMotivo(), estilos.datoTexto);
                    crearCelda(fila, 12, c.getObservacion(), estilos.datoTexto);
                }
            } else {
                Row vacia = hojaDetalle.createRow(nroFila++);
                crearCelda(vacia, 0, "No hay citas registradas para el período seleccionado.", estilos.info);
                hojaDetalle.addMergedRegion(new CellRangeAddress(nroFila - 1, nroFila - 1, 0, COLUMNAS_DETALLE.length - 1));
            }

            // =========================================================================
            // HOJA 2: RESUMEN POR ESTADO
            // =========================================================================
            Sheet hojaResumen = libro.createSheet("Resumen Estadístico");

            Row titRes = hojaResumen.createRow(0);
            crearCelda(titRes, 0, razonSocialEmpresa + " - Resumen Estadístico por Estado", estilos.titulo);
            hojaResumen.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            crearCelda(hojaResumen.createRow(1), 0, "Período: " + reporte.getFechaInicio() + " al " + reporte.getFechaFin(), estilos.info);

            String[] colRes = {"Estado de la Cita", "Cantidad de Citas", "Porcentaje"};
            int[] anchRes = {24, 20, 18};

            Row cabRes = hojaResumen.createRow(3);
            for (int i = 0; i < colRes.length; i++) {
                crearCelda(cabRes, i, colRes[i], estilos.cabecera);
                hojaResumen.setColumnWidth(i, anchRes[i] * 256);
            }

            int filaRes = 4;
            Map<String, Long> desglose = reporte.getDesglosePorEstado();
            long totalCitas = reporte.getTotalCitas();

            if (desglose != null && !desglose.isEmpty()) {
                for (Map.Entry<String, Long> entry : desglose.entrySet()) {
                    Row fila = hojaResumen.createRow(filaRes++);
                    crearCelda(fila, 0, entry.getKey(), estilos.datoTexto);
                    crearCelda(fila, 1, entry.getValue(), estilos.datoNumero);
                    double pct = totalCitas > 0 ? ((double) entry.getValue() / totalCitas) * 100.0 : 0.0;
                    crearCelda(fila, 2, String.format("%.1f%%", pct), estilos.datoCentro);
                }
            }

            Row filaTotal = hojaResumen.createRow(filaRes);
            crearCelda(filaTotal, 0, "TOTAL GENERAL", estilos.total);
            crearCelda(filaTotal, 1, totalCitas, estilos.total);
            crearCelda(filaTotal, 2, "100.0%", estilos.total);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de citas", e);
        }
    }

    private void crearCelda(Row fila, int col, Object valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        if (valor instanceof Number num) {
            celda.setCellValue(num.doubleValue());
        } else if (valor != null) {
            celda.setCellValue(valor.toString());
        } else {
            celda.setCellValue("-");
        }
        celda.setCellStyle(estilo);
    }

    private static class Estilos {
        final CellStyle titulo;
        final CellStyle info;
        final CellStyle cabecera;
        final CellStyle datoTexto;
        final CellStyle datoCentro;
        final CellStyle datoNumero;
        final CellStyle total;

        Estilos(XSSFWorkbook libro) {
            XSSFFont fTitulo = libro.createFont();
            fTitulo.setFontHeightInPoints((short) 13);
            fTitulo.setBold(true);

            titulo = libro.createCellStyle();
            titulo.setFont(fTitulo);

            XSSFFont fInfo = libro.createFont();
            fInfo.setFontHeightInPoints((short) 9);
            fInfo.setItalic(true);

            info = libro.createCellStyle();
            info.setFont(fInfo);

            XSSFFont fCab = libro.createFont();
            fCab.setBold(true);
            fCab.setColor(COLOR_BLANCO);

            cabecera = libro.createCellStyle();
            ((XSSFCellStyle) cabecera).setFillForegroundColor(COLOR_CABECERA);
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            cabecera.setFont(fCab);
            aplicarBordes(cabecera);

            datoTexto = libro.createCellStyle();
            aplicarBordes(datoTexto);

            datoCentro = libro.createCellStyle();
            datoCentro.setAlignment(HorizontalAlignment.CENTER);
            aplicarBordes(datoCentro);

            datoNumero = libro.createCellStyle();
            datoNumero.setAlignment(HorizontalAlignment.RIGHT);
            aplicarBordes(datoNumero);

            XSSFFont fTotal = libro.createFont();
            fTotal.setBold(true);

            total = libro.createCellStyle();
            ((XSSFCellStyle) total).setFillForegroundColor(COLOR_FONDO_TOTAL);
            total.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            total.setAlignment(HorizontalAlignment.CENTER);
            total.setFont(fTotal);
            aplicarBordes(total);
        }

        private void aplicarBordes(CellStyle estilo) {
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
        }
    }
}
