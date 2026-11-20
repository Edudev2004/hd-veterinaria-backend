package com.vethd.backend.report;

import com.vethd.backend.DTOs.response.report.MascotasEspecieResponse;
import com.vethd.backend.model.Pet;
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
public class MascotaExcelGenerator {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final XSSFColor COLOR_CABECERA = new XSSFColor(new byte[]{(byte) 13, (byte) 148, (byte) 136}, null);
    private static final XSSFColor COLOR_BLANCO = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
    private static final XSSFColor COLOR_FONDO_TOTAL = new XSSFColor(new byte[]{(byte) 240, (byte) 253, (byte) 250}, null);

    private final String razonSocialEmpresa;

    public MascotaExcelGenerator(@Value("${app.empresa.razon-social:VetHD - Plataforma Clínica Veterinaria}") String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public byte[] generar(List<MascotasEspecieResponse> especies, List<Pet> pacientes) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);

            // =========================================================================
            // HOJA 1: DISTRIBUCIÓN POR ESPECIE
            // =========================================================================
            Sheet hojaEspecies = libro.createSheet("Especies");
            String[] colEsp = {"Posición", "Especie Animal", "Cantidad de Mascotas", "Porcentaje de Población"};
            int[] anchEsp = {12, 24, 24, 26};

            Row titEsp = hojaEspecies.createRow(0);
            crearCelda(titEsp, 0, razonSocialEmpresa + " - Reporte Demográfico de Especies", estilos.titulo);
            hojaEspecies.addMergedRegion(new CellRangeAddress(0, 0, 0, colEsp.length - 1));

            long totalMascotas = especies != null ? especies.stream().mapToLong(MascotasEspecieResponse::getCantidad).sum() : 0L;
            int totalEspecies = especies != null ? especies.size() : 0;

            crearCelda(hojaEspecies.createRow(1), 0, "Generado: " + LocalDateTime.now().format(FORMATO_FECHA), estilos.info);
            crearCelda(hojaEspecies.createRow(2), 0, "Total de mascotas activas: " + totalMascotas + "   |   Especies registradas: " + totalEspecies, estilos.info);

            Row cabEsp = hojaEspecies.createRow(4);
            for (int i = 0; i < colEsp.length; i++) {
                crearCelda(cabEsp, i, colEsp[i], estilos.cabecera);
                hojaEspecies.setColumnWidth(i, anchEsp[i] * 256);
            }

            int fEsp = 5;
            if (especies != null && !especies.isEmpty()) {
                int pos = 1;
                for (MascotasEspecieResponse e : especies) {
                    Row fila = hojaEspecies.createRow(fEsp++);
                    crearCelda(fila, 0, pos++, estilos.datoCentro);
                    crearCelda(fila, 1, e.getEspecie() != null ? e.getEspecie().substring(0, 1).toUpperCase() + e.getEspecie().substring(1) : "-", estilos.datoTexto);
                    crearCelda(fila, 2, e.getCantidad(), estilos.datoNumero);
                    double pct = totalMascotas > 0 ? ((double) e.getCantidad() / totalMascotas) * 100.0 : 0.0;
                    crearCelda(fila, 3, String.format("%.1f%%", pct), estilos.datoCentro);
                }
            } else {
                Row vacia = hojaEspecies.createRow(fEsp++);
                crearCelda(vacia, 0, "No hay mascotas registradas para mostrar.", estilos.info);
                hojaEspecies.addMergedRegion(new CellRangeAddress(fEsp - 1, fEsp - 1, 0, colEsp.length - 1));
            }

            Row filaTotal = hojaEspecies.createRow(fEsp);
            crearCelda(filaTotal, 0, "TOTAL GENERAL", estilos.total);
            hojaEspecies.addMergedRegion(new CellRangeAddress(fEsp, fEsp, 0, 1));
            crearCelda(filaTotal, 2, totalMascotas, estilos.total);
            crearCelda(filaTotal, 3, "100.0%", estilos.total);

            // =========================================================================
            // HOJA 2: PADRÓN DE PACIENTES
            // =========================================================================
            if (pacientes != null && !pacientes.isEmpty()) {
                Sheet hojaPacientes = libro.createSheet("Padrón de Pacientes");
                String[] colPac = {"N°", "Nombre de Mascota", "Especie", "Raza", "Sexo", "Propietario", "Teléfono Contacto"};
                int[] anchPac = {8, 22, 16, 20, 12, 28, 20};

                Row titPac = hojaPacientes.createRow(0);
                crearCelda(titPac, 0, razonSocialEmpresa + " - Padrón de Pacientes Registrados", estilos.titulo);
                hojaPacientes.addMergedRegion(new CellRangeAddress(0, 0, 0, colPac.length - 1));

                Row cabPac = hojaPacientes.createRow(2);
                for (int i = 0; i < colPac.length; i++) {
                    crearCelda(cabPac, i, colPac[i], estilos.cabecera);
                    hojaPacientes.setColumnWidth(i, anchPac[i] * 256);
                }

                int fPac = 3;
                int pIndex = 1;
                for (Pet p : pacientes) {
                    Row fila = hojaPacientes.createRow(fPac++);
                    String propNombre = (p.getOwner() != null && p.getOwner().getUser() != null)
                            ? (p.getOwner().getUser().getFirstName() + " " + p.getOwner().getUser().getLastName()).trim()
                            : "No asignado";
                    String propTel = (p.getOwner() != null && p.getOwner().getPhone() != null)
                            ? p.getOwner().getPhone()
                            : "-";

                    crearCelda(fila, 0, pIndex++, estilos.datoCentro);
                    crearCelda(fila, 1, p.getName(), estilos.datoTexto);
                    crearCelda(fila, 2, p.getSpecies(), estilos.datoCentro);
                    crearCelda(fila, 3, p.getBreed() != null ? p.getBreed() : "Mestizo", estilos.datoTexto);
                    crearCelda(fila, 4, p.getGender() != null ? p.getGender() : "-", estilos.datoCentro);
                    crearCelda(fila, 5, propNombre, estilos.datoTexto);
                    crearCelda(fila, 6, propTel, estilos.datoCentro);
                }
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel de especies", e);
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
