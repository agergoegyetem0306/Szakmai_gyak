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
            Row dataRow = sheet.getRow(1);

            if (headerRow == null) {
                response.put("error", "Az Excel fájl nem tartalmaz fejlécsort.");
                return ResponseEntity.badRequest().body(response);
            }

            if (dataRow == null) {
                response.put("error", "Az Excel fájl nem tartalmaz adatsort.");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            List<String> excelHeaders = readHeaderCells(headerRow);
            List<String> mappedColumns = new ArrayList<>();
            List<String> unmappedColumns = new ArrayList<>();

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

                if (!crosswalk.containsKey(header)) {
                    unmappedColumns.add(header);
                    continue;
                }

                mappedColumns.add(header);

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

            response.put("fileName", fileName);
            response.put("sheetName", sheet.getSheetName());
            response.put("excelHeaders", excelHeaders);
            response.put("mappedColumns", mappedColumns);
            response.put("unmappedColumns", unmappedColumns);
            response.put("xml", xml.toString());
            response.put("message", "Dublin Core XML generálása sikeres volt az első adatsor alapján.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Hiba történt a Dublin Core XML generálása során: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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