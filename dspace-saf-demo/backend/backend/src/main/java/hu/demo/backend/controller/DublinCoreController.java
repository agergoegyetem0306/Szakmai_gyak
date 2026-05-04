package hu.demo.backend.controller;

import hu.demo.backend.service.CrosswalkService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/dublin-core")
public class DublinCoreController {

    private static final Set<String> TECHNICAL_COLUMNS = Set.of("Mappa");

    private final CrosswalkService crosswalkService;

    public DublinCoreController(CrosswalkService crosswalkService) {
        this.crosswalkService = crosswalkService;
    }

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> generateDublinCore(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (file.isEmpty()) {
            response.put("error", "Nincs feltöltött fájl.");
            return ResponseEntity.badRequest().body(response);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            response.put("error", "Csak .xlsx Excel fájl támogatott.");
            return ResponseEntity.badRequest().body(response);
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                response.put("error", "Az Excel fájl nem tartalmaz fejlécsort.");
                return ResponseEntity.badRequest().body(response);
            }

            if (sheet.getLastRowNum() < 1) {
                response.put("error", "Az Excel fájl nem tartalmaz adatsort.");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            List<String> excelHeaders = readHeaderCells(headerRow);
            List<String> mappedColumns = new ArrayList<>();
            List<String> unmappedColumns = new ArrayList<>();
            List<String> technicalColumns = new ArrayList<>();

            short lastCellNum = headerRow.getLastCellNum();

            for (int i = 0; i < lastCellNum; i++) {
                Cell headerCell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (headerCell == null || headerCell.getCellType() == CellType.BLANK) {
                    continue;
                }

                String header = getCellValueAsString(headerCell).trim();

                if (header.isEmpty()) {
                    continue;
                }

                if (TECHNICAL_COLUMNS.contains(header)) {
                    technicalColumns.add(header);
                } else if (crosswalk.containsKey(header)) {
                    mappedColumns.add(header);
                } else {
                    unmappedColumns.add(header);
                }
            }

            List<Map<String, Object>> items = new ArrayList<>();
            int itemIndex = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);

                if (isRowEmpty(dataRow)) {
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                String itemName = String.format("item_%03d", itemIndex);

                String folderPath = findTechnicalValue(headerRow, dataRow, "Mappa");

                String xml = generateXmlForRow(headerRow, dataRow, crosswalk);

                item.put("itemName", itemName);
                item.put("sourceRowNumber", rowIndex + 1);
                item.put("folderPath", folderPath);
                item.put("xml", xml);

                items.add(item);
                itemIndex++;
            }

            response.put("fileName", fileName);
            response.put("sheetName", sheet.getSheetName());
            response.put("excelHeaders", excelHeaders);
            response.put("mappedColumns", mappedColumns);
            response.put("unmappedColumns", unmappedColumns);
            response.put("technicalColumns", technicalColumns);
            response.put("itemCount", items.size());
            response.put("items", items);
            response.put("message", "Dublin Core XML generálása sikeres volt minden adatsor alapján.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Hiba történt a Dublin Core XML generálása során: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/generate-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateSafZip(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().build();
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(baos)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null || sheet.getLastRowNum() < 1) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            int itemIndex = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);

                if (isRowEmpty(dataRow)) {
                    continue;
                }

                String itemName = String.format("item_%03d", itemIndex);

                String xml = generateXmlForRow(headerRow, dataRow, crosswalk);

                String entryName = itemName + "/dublin_core.xml";

                zipOut.putNextEntry(new java.util.zip.ZipEntry(entryName));
                zipOut.write(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zipOut.closeEntry();

                itemIndex++;
            }

            zipOut.finish();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"saf_export.zip\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateXmlForRow(Row headerRow, Row dataRow, Map<String, String> crosswalk) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
        xml.append("<dublin_core schema=\"dc\">\n");

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell headerCell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (headerCell == null || headerCell.getCellType() == CellType.BLANK) {
                continue;
            }

            String header = getCellValueAsString(headerCell).trim();

            if (header.isEmpty()) {
                continue;
            }

            if (TECHNICAL_COLUMNS.contains(header)) {
                continue;
            }

            if (!crosswalk.containsKey(header)) {
                continue;
            }

            Cell valueCell = dataRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (valueCell == null || valueCell.getCellType() == CellType.BLANK) {
                continue;
            }

            String value = getCellValueAsString(valueCell).trim();

            if (value.isEmpty()) {
                continue;
            }

            String dublinCoreField = crosswalk.get(header);
            String[] dcParts = dublinCoreField.split("\\.");

            if (dcParts.length != 2) {
                throw new IllegalArgumentException("Hibás Dublin Core mező a crosswalkban: " + dublinCoreField);
            }

            String element = dcParts[0].trim();
            String qualifier = dcParts[1].trim();

            xml.append("  <dcvalue element=\"")
                    .append(escapeXml(element))
                    .append("\" qualifier=\"")
                    .append(escapeXml(qualifier))
                    .append("\" language=\"hu\">")
                    .append(escapeXml(value))
                    .append("</dcvalue>\n");
        }

        xml.append("</dublin_core>");

        return xml.toString();
    }

    private String findTechnicalValue(Row headerRow, Row dataRow, String columnName) {
        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell headerCell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (headerCell == null || headerCell.getCellType() == CellType.BLANK) {
                continue;
            }

            String header = getCellValueAsString(headerCell).trim();

            if (!columnName.equals(header)) {
                continue;
            }

            Cell valueCell = dataRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (valueCell == null || valueCell.getCellType() == CellType.BLANK) {
                return "";
            }

            return normalizePath(getCellValueAsString(valueCell).trim());
        }

        return "";
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }

        String normalized = path.trim();

        while (normalized.startsWith("\\") || normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("\\") || normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private List<String> readHeaderCells(Row headerRow) {
        List<String> headers = new ArrayList<>();

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell).trim();

                if (!value.isEmpty()) {
                    headers.add(value);
                }
            }
        }

        return headers;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        short lastCellNum = row.getLastCellNum();

        if (lastCellNum < 0) {
            return true;
        }

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell).trim();

                if (!value.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private String getCellValueAsString(Cell cell) {
        DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("hu"));
        return formatter.formatCellValue(cell);
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}