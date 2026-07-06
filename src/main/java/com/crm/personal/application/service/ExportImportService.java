package com.crm.personal.application.service;

import com.crm.personal.application.dto.ContactoDTO;
import com.crm.personal.application.dto.ImportResultDTO;
import com.crm.personal.domain.model.*;
import com.crm.personal.domain.repository.CampoDinamicoRepository;
import com.crm.personal.domain.repository.ContactoRepository;
import com.crm.personal.domain.repository.TimelineRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Servicio de exportación e importación masiva.
 *
 * <ul>
 *   <li>Exporta el expediente completo de un contacto a PDF (PDFBox 3.x)</li>
 *   <li>Genera una plantilla Excel vacía con columnas correctas (Apache POI)</li>
 *   <li>Importa contactos masivamente desde un archivo Excel</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ExportImportService {

    private static final Logger log = LoggerFactory.getLogger(ExportImportService.class);
    private static final DateTimeFormatter DATE_FMT      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");

    private final ContactoRepository      contactoRepo;
    private final CampoDinamicoRepository campoRepo;
    private final TimelineRecordRepository timelineRepo;
    private final ContactoService         contactoService;

    @Value("${app.data.dir}")
    private String dataDir;

    public ExportImportService(ContactoRepository contactoRepo,
                               CampoDinamicoRepository campoRepo,
                               TimelineRecordRepository timelineRepo,
                               ContactoService contactoService) {
        this.contactoRepo   = contactoRepo;
        this.campoRepo      = campoRepo;
        this.timelineRepo   = timelineRepo;
        this.contactoService = contactoService;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPORTAR A PDF
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Genera un PDF completo del expediente de un contacto.
     *
     * @param contactoId ID del contacto
     * @return contenido del PDF como arreglo de bytes
     */
    public byte[] exportContactoToPdf(Long contactoId) throws IOException {
        Contacto contacto = contactoRepo.findById(contactoId)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + contactoId));
        List<TimelineRecord> timeline = timelineRepo.findByContactoIdOrderByFechaAsc(contactoId);

        try (PDDocument doc = new PDDocument()) {
            PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontItalic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin     = 50f;
            float pageWidth  = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float yPos;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // ── Banner de encabezado ────────────────────────────────────
                cs.setNonStrokingColor(0.42f, 0.39f, 1.0f);   // #6C63FF
                cs.addRect(0, pageHeight - 75, pageWidth, 75);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 22);
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.newLineAtOffset(margin, pageHeight - 48);
                cs.showText("CRM Personal \u2014 Expediente de Contacto");
                cs.endText();

                yPos = pageHeight - 95;

                // ── Foto de perfil ──────────────────────────────────────────
                if (contacto.getFotoPerfilPath() != null) {
                    try {
                        java.io.File imgFile = new java.io.File(contacto.getFotoPerfilPath());
                        if (imgFile.exists()) {
                            PDImageXObject img = PDImageXObject.createFromFile(
                                contacto.getFotoPerfilPath(), doc);
                            cs.drawImage(img, margin, yPos - 90, 90, 90);
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo cargar la foto de perfil: {}", e.getMessage());
                    }
                }

                // ── Datos del contacto ──────────────────────────────────────
                float xData = margin + 110;
                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                cs.newLineAtOffset(xData, yPos - 16);
                cs.showText(safe(contacto.getNombre()));
                cs.setFont(fontNormal, 12);
                cs.setLeading(18f);
                cs.newLine();
                cs.showText("DNI: " + safe(contacto.getDni()));
                cs.newLine();
                cs.showText("Direcci\u00f3n: " + safe(contacto.getDireccion()));
                cs.endText();

                yPos -= 110;

                // ── Campos dinámicos ────────────────────────────────────────
                if (!contacto.getCamposDinamicos().isEmpty()) {
                    cs.beginText();
                    cs.setFont(fontBold, 11);
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.newLineAtOffset(margin, yPos);
                    cs.showText("Campos personalizados:");
                    cs.endText();
                    yPos -= 14;
                    for (CampoDinamicoValor v : contacto.getCamposDinamicos()) {
                        cs.beginText();
                        cs.setFont(fontNormal, 10);
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        cs.newLineAtOffset(margin + 10, yPos);
                        cs.showText(safe(v.getCampo().getNombre()) + ": " + safe(v.getValor()));
                        cs.endText();
                        yPos -= 13;
                    }
                    yPos -= 5;
                }

                // ── Etiquetas ───────────────────────────────────────────────
                if (!contacto.getEtiquetas().isEmpty()) {
                    String tags = contacto.getEtiquetas().stream()
                        .map(Etiqueta::getNombre)
                        .reduce((a, b) -> a + ", " + b).orElse("");
                    cs.beginText();
                    cs.setFont(fontBold, 11);
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.newLineAtOffset(margin, yPos);
                    cs.showText("Etiquetas: ");
                    cs.setFont(fontNormal, 11);
                    cs.showText(safe(tags));
                    cs.endText();
                    yPos -= 20;
                }

                // ── Separador ───────────────────────────────────────────────
                cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                cs.moveTo(margin, yPos);
                cs.lineTo(pageWidth - margin, yPos);
                cs.stroke();
                yPos -= 20;

                // ── Título timeline ──────────────────────────────────────────
                cs.beginText();
                cs.setFont(fontBold, 14);
                cs.setNonStrokingColor(0.42f, 0.39f, 1.0f);
                cs.newLineAtOffset(margin, yPos);
                cs.showText("Expediente / Timeline");
                cs.endText();
                yPos -= 25;

                // ── Registros ───────────────────────────────────────────────
                for (TimelineRecord record : timeline) {
                    if (yPos < 120) break; // página siguiente no implementada (extensión futura)

                    // Fecha + tipo
                    cs.beginText();
                    cs.setFont(fontItalic, 9);
                    cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                    cs.newLineAtOffset(margin, yPos);
                    String fechaStr = record.getFecha() != null
                        ? record.getFecha().format(DATE_FMT) : "Sin fecha";
                    cs.showText(fechaStr + "  [" + record.getTipo().name() + "]");
                    cs.endText();
                    yPos -= 14;

                    // Título del record
                    cs.beginText();
                    cs.setFont(fontBold, 11);
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.newLineAtOffset(margin + 10, yPos);
                    cs.showText(safe(record.getTitulo()));
                    cs.endText();
                    yPos -= 14;

                    // Contenido HTML → texto plano
                    if (record.getContenidoHtml() != null && !record.getContenidoHtml().isBlank()) {
                        String plain = htmlToPlain(record.getContenidoHtml());
                        List<String> lines = wrap(plain, 88);
                        cs.beginText();
                        cs.setFont(fontNormal, 10);
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        cs.setLeading(14f);
                        cs.newLineAtOffset(margin + 10, yPos);
                        for (String line : lines) {
                            if (yPos < 100) break;
                            cs.showText(safe(line));
                            cs.newLine();
                            yPos -= 14;
                        }
                        cs.endText();
                    }

                    // Adjuntos multimedia (thumbnails 80x80)
                    for (MediaAdjunto adj : record.getAdjuntos()) {
                        if (yPos < 120) break;
                        try {
                            java.io.File imgFile = new java.io.File(adj.getFilePath());
                            if (imgFile.exists() && adj.getTipoMime() != null
                                    && adj.getTipoMime().startsWith("image/")) {
                                PDImageXObject img = PDImageXObject.createFromFile(
                                    adj.getFilePath(), doc);
                                cs.drawImage(img, margin + 10, yPos - 80, 80, 80);

                                cs.beginText();
                                cs.setFont(fontNormal, 9);
                                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                                cs.setLeading(13f);
                                cs.newLineAtOffset(margin + 100, yPos - 10);
                                if (adj.getDescripcion() != null)
                                    cs.showText(safe(adj.getDescripcion()));
                                cs.newLine();
                                if (adj.getLugar() != null)
                                    cs.showText("Lugar: " + safe(adj.getLugar()));
                                cs.newLine();
                                if (adj.getFechaCaptura() != null)
                                    cs.showText("Fecha: " + adj.getFechaCaptura().format(DATE_ONLY_FMT));
                                cs.endText();
                                yPos -= 90;
                            }
                        } catch (Exception e) {
                            log.warn("Error al insertar imagen en PDF: {}", e.getMessage());
                        }
                    }
                    yPos -= 10;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            log.info("PDF generado para contacto {} ({} bytes)", contactoId, baos.size());
            return baos.toByteArray();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PLANTILLA EXCEL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Genera una plantilla Excel vacía lista para rellenar e importar.
     */
    public byte[] generarPlantillaExcel() throws IOException {
        List<CampoDinamico> campos = campoRepo.findByActivoTrue();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Estilo de encabezado ────────────────────────────────────────
            CellStyle headerStyle = wb.createCellStyle();
            Font hFont = wb.createFont();
            hFont.setBold(true);
            hFont.setFontHeightInPoints((short) 11);
            hFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            // ── Estilo de ejemplo ───────────────────────────────────────────
            CellStyle exStyle = wb.createCellStyle();
            Font exFont = wb.createFont();
            exFont.setItalic(true);
            exFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            exStyle.setFont(exFont);

            // ── Hoja principal ──────────────────────────────────────────────
            Sheet sheet = wb.createSheet("Contactos");

            List<String> headers = new ArrayList<>(List.of("Nombre *", "DNI * (8 dígitos)", "Dirección *"));
            for (CampoDinamico c : campos) headers.add(c.getNombre() + " (" + c.getTipo() + ")");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 7000);
            }

            // Fila de ejemplo (itálica gris)
            Row exRow = sheet.createRow(1);
            Cell e0 = exRow.createCell(0); e0.setCellValue("Juan Pérez");  e0.setCellStyle(exStyle);
            Cell e1 = exRow.createCell(1); e1.setCellValue("12345678");     e1.setCellStyle(exStyle);
            Cell e2 = exRow.createCell(2); e2.setCellValue("Av. Lima 123"); e2.setCellStyle(exStyle);
            for (int i = 0; i < campos.size(); i++) {
                Cell c = exRow.createCell(3 + i);
                c.setCellValue("Ejemplo " + campos.get(i).getNombre());
                c.setCellStyle(exStyle);
            }

            // ── Hoja de instrucciones ───────────────────────────────────────
            Sheet instrSheet = wb.createSheet("Instrucciones");
            instrSheet.setColumnWidth(0, 22000);
            String[] instrucciones = {
                "INSTRUCCIONES DE IMPORTACIÓN",
                "",
                "• Los campos marcados con * son OBLIGATORIOS.",
                "• El DNI debe tener exactamente 8 dígitos numéricos (ej: 12345678).",
                "• No modifiques la fila de encabezados (fila 1).",
                "• La fila 2 es un ejemplo — puedes eliminarla o dejarla.",
                "• Los registros con DNI duplicado serán omitidos con un mensaje de error.",
                "• Guarda el archivo como .xlsx antes de importar."
            };
            for (int i = 0; i < instrucciones.length; i++) {
                instrSheet.createRow(i).createCell(0).setCellValue(instrucciones[i]);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            log.info("Plantilla Excel generada con {} campos dinámicos", campos.size());
            return baos.toByteArray();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  IMPORTAR DESDE EXCEL
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Importa contactos masivamente desde un archivo Excel (.xlsx).
     *
     * @param inputStream stream del archivo Excel
     * @return resultado con conteo de importados y mensajes de error
     */
    @Transactional
    public ImportResultDTO importarDesdeExcel(InputStream inputStream) {
        ImportResultDTO resultado = new ImportResultDTO();

        try (XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheet("Contactos");
            if (sheet == null) {
                resultado.addError("El archivo no contiene la hoja 'Contactos'.");
                return resultado;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                resultado.addError("El archivo no tiene fila de encabezados.");
                return resultado;
            }

            // Mapear columna → CampoDinamico
            List<CampoDinamico> camposActivos = campoRepo.findByActivoTrue();
            Map<Integer, CampoDinamico> colToCampo = new HashMap<>();
            for (int col = 3; col < headerRow.getLastCellNum(); col++) {
                Cell h = headerRow.getCell(col);
                if (h == null) continue;
                String hText = h.getStringCellValue().trim();
                for (CampoDinamico campo : camposActivos) {
                    if (hText.startsWith(campo.getNombre())) {
                        colToCampo.put(col, campo);
                        break;
                    }
                }
            }

            // Procesar filas de datos
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                String nombre    = cellStr(row, 0);
                String dni       = cellStr(row, 1);
                String direccion = cellStr(row, 2);

                if (nombre.isBlank() && dni.isBlank()) continue;

                // Omitir fila de ejemplo
                if (nombre.equalsIgnoreCase("Juan Pérez") && dni.equals("12345678")) continue;

                // Validaciones
                if (nombre.isBlank()) {
                    resultado.addError("Fila " + (rowIdx + 1) + ": Nombre obligatorio.");
                    continue;
                }
                if (!dni.matches("\\d{8}")) {
                    resultado.addError("Fila " + (rowIdx + 1) + ": DNI inválido '" + dni +
                        "'. Debe tener exactamente 8 dígitos.");
                    continue;
                }
                if (direccion.isBlank()) {
                    resultado.addError("Fila " + (rowIdx + 1) + ": Dirección obligatoria.");
                    continue;
                }

                // Campos dinámicos
                Map<Long, String> camposMap = new HashMap<>();
                for (Map.Entry<Integer, CampoDinamico> e : colToCampo.entrySet()) {
                    String val = cellStr(row, e.getKey());
                    if (!val.isBlank()) camposMap.put(e.getValue().getId(), val);
                }

                ContactoDTO dto = ContactoDTO.builder()
                    .nombre(nombre).dni(dni).direccion(direccion)
                    .camposDinamicos(camposMap)
                    .build();

                try {
                    contactoService.save(dto);
                    resultado.addImportado();
                } catch (IllegalArgumentException ex) {
                    resultado.addError("Fila " + (rowIdx + 1) + ": " + ex.getMessage());
                } catch (Exception ex) {
                    resultado.addError("Fila " + (rowIdx + 1) + ": Error inesperado — " + ex.getMessage());
                    log.error("Error importando fila {}", rowIdx + 1, ex);
                }
            }
        } catch (IOException e) {
            resultado.addError("Error al leer el archivo Excel: " + e.getMessage());
            log.error("Error leyendo Excel", e);
        }

        log.info("Importación: {} ok, {} errores", resultado.getImportados(), resultado.getErrores());
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilidades privadas
    // ═══════════════════════════════════════════════════════════════════════

    private String cellStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> "";
        };
    }

    private String htmlToPlain(String html) {
        if (html == null) return "";
        return HTML_TAGS.matcher(html)
                .replaceAll("")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
    }

    /** Parte texto largo en líneas de máximo maxChars caracteres. */
    private List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) return lines;
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxChars) {
                if (!line.isEmpty()) lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (!line.isEmpty()) line.append(' ');
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    /** Elimina caracteres no soportados por las fuentes Type1 de PDFBox. */
    private String safe(String text) {
        if (text == null) return "";
        return text.replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", "?");
    }
}
