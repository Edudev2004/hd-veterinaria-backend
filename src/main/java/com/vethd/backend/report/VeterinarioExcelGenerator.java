package com.vethd.backend.report;

import com.vethd.backend.DTOs.response.report.VeterinarioRankingResponse;
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

@Component
public class VeterinarioExcelGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 13, (byte) 148, (byte) 136}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
    private static final XSSFColor COLOR_FONDO_TOTAL = new XSSFColor(new byte[]{(byte) 240, (byte) 253, (byte) 250}, null);

    private static final String[] COLUMNAS = {
            "Posición", "Médico Veterinario", "DNI", "Especialidades", "Correo Electrónico", "Estado", "Citas Atendidas", "Participación"
    };

    private static final int[] ANCHOS = {12, 28, 14, 28, 30, 14, 18, 18};
    private static final int FILA_CABECERA = 4;

    private final String razonSocialEmpresa;

    public VeterinarioExcelGenerator(@Value("${app.empresa.razon-social:VetHD - Plataforma Clínica Veterinaria}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(List<VeterinarioRankingResponse> ranking) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            Sheet hoja = libro.createSheet("Ranking Veterinarios");

            // Título
            Row titulo = hoja.createRow(0);
            crearCelda(titulo, 0, razonSocialEmpresa + " - Reporte y Ranking del Cuerpo Médico", estilos.titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));

            long totalAtenciones = ranking != null ? ranking.stream().mapToLong(VeterinarioRankingResponse::getCitasAtendidas).sum() : 0L;
            int totalVets = ranking != null ? ranking.size() : 0;

            crearCelda(hoja.createRow(1), 0, "Generado: " + LocalDateTime.now().format(FORMATO_FECHA), estilos.info);
            crearCelda(hoja.createRow(2), 0, "Total de médicos veterinarios: " + totalVets + "   |   Total de atenciones atendidas: " + totalAtenciones, estilos.info);

            // Cabecera
            Row cabecera = hoja.createRow(FILA_CABECERA);
            for (int i = 0; i < COLUMNAS.length; i++) {
                crearCelda(cabecera, i, COLUMNAS[i], estilos.cabecera);
                hoja.setColumnWidth(i, ANCHOS[i] * 256);
            }

            int nroFila = FILA_CABECERA + 1;
            if (ranking != null && !ranking.isEmpty()) {
                int pos = 1;
                for (VeterinarioRankingResponse v : ranking) {
                    Row fila = hoja.createRow(nroFila++);
                    crearCelda(fila, 0, pos++, estilos.datoCentro);
                    crearCelda(fila, 1, v.getNombreCompleto(), estilos.datoTexto);
                    crearCelda(fila, 2, "-", estilos.datoCentro);
                    crearCelda(fila, 3, v.getEspecialidad() != null ? v.getEspecialidad() : "Medicina General", estilos.datoTexto);
                    crearCelda(fila, 4, v.getEmail() != null ? v.getEmail() : "-", estilos.datoTexto);
                    crearCelda(fila, 5, Boolean.FALSE.equals(v.getActivo()) ? "Inactivo" : "Activo", estilos.datoCentro);
                    crearCelda(fila, 6, v.getCitasAtendidas(), estilos.datoNumero);
                    double pct = totalAtenciones > 0 ? ((double) v.getCitasAtendidas() / totalAtenciones) * 100.0 : 0.0;
                    crearCelda(fila, 7, String.format("%.1f%%", pct), estilos.datoCentro);
                }
            } else {
                Row vacia = hoja.createRow(nroFila++);
                crearCelda(vacia, 0, "No hay veterinarios registrados para mostrar.", estilos.info);
                hoja.addMergedRegion(new CellRangeAddress(nroFila - 1, nroFila - 1, 0, COLUMNAS.length - 1));
            }

            // Fila Total
            Row filaTotal = hoja.createRow(nroFila);
            crearCelda(filaTotal, 0, "TOTAL GENERAL", estilos.total);
            hoja.addMergedRegion(new CellRangeAddress(nroFila, nroFila, 0, 5));
            crearCelda(filaTotal, 6, totalAtenciones, estilos.total);
            crearCelda(filaTotal, 7, "100.0%", estilos.total);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de veterinarios", e);
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
