package hu.demo.backend.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
public class DublinCoreService {

    private final CrosswalkService crosswalkService;
    private final ColumnClassificationService columnClassificationService;

    public DublinCoreService(
            CrosswalkService crosswalkService,
            ColumnClassificationService columnClassificationService
    ) {
        this.crosswalkService = crosswalkService;
        this.columnClassificationService = columnClassificationService;
    }

    public Map<String, Object> generateDublinCoreItems(MultipartFile file) {
        Map<String, Object> response = new LinkedHashMap<>();

        validateExcelFile(file);

        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Az Excel fájl nem tartalmaz fejlécsort.");
            }

            if (sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("Az Excel fájl nem tartalmaz adatsort.");
            }

            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            List<String> excelHeaders = readHeaderCells(headerRow);
            List<String> mappedColumns = new ArrayList<>();
            List<String> unmappedColumns = new ArrayList<>();
            List<String> technicalColumns = new ArrayList<>();
            List<String> bundleColumns = new ArrayList<>();

            classifyColumns(
                    excelHeaders,
                    crosswalk,
                    mappedColumns,
                    unmappedColumns,
                    technicalColumns,
                    bundleColumns
            );

            List<Map<String, Object>> items = generateItems(sheet, headerRow, crosswalk);

            response.put("fileName", fileName);
            response.put("sheetName", sheet.getSheetName());
            response.put("excelHeaders", excelHeaders);
            response.put("mappedColumns", mappedColumns);
            response.put("unmappedColumns", unmappedColumns);
            response.put("technicalColumns", technicalColumns);
            response.put("bundleColumns", bundleColumns);
            response.put("itemCount", items.size());
            response.put("items", items);
            response.put("message", "Dublin Core XML generálása sikeres volt minden adatsor alapján.");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a Dublin Core XML generálása során: " + e.getMessage(), e);
        }
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Nincs feltöltött fájl.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Csak .xlsx Excel fájl támogatott.");
        }
    }

    private void classifyColumns(
            List<String> excelHeaders,
            Map<String, String> crosswalk,
            List<String> mappedColumns,
            List<String> unmappedColumns,
            List<String> technicalColumns,
            List<String> bundleColumns
    ) {
        for (String header : excelHeaders) {
            if (columnClassificationService.isTechnicalColumn(header)) {
                technicalColumns.add(header);
            } else if (columnClassificationService.isBundleColumn(header)) {
                bundleColumns.add(header);
            } else if (crosswalk.containsKey(header)) {
                mappedColumns.add(header);
            } else {
                unmappedColumns.add(header);
            }
        }
    }

    private List<Map<String, Object>> generateItems(
            Sheet sheet,
            Row headerRow,
            Map<String, String> crosswalk
    ) {
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

        return items;
    }

    public String generateXmlForRow(Row headerRow, Row dataRow, Map<String, String> crosswalk) {
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

            if (columnClassificationService.isSystemColumn(header)) {
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