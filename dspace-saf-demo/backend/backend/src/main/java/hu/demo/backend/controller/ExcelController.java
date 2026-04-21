package hu.demo.backend.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> analyzeExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

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

            if (sheet == null) {
                response.put("error", "Az Excel fájl nem tartalmaz munkalapot.");
                return ResponseEntity.badRequest().body(response);
            }

            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                response.put("error", "Az Excel fájl üres.");
                return ResponseEntity.badRequest().body(response);
            }

            Row headerRow = rowIterator.next();
            int columnCount = countNonEmptyHeaderCells(headerRow);

            int dataRowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (!isRowEmpty(row)) {
                    dataRowCount++;
                }
            }

            response.put("fileName", fileName);
            response.put("sheetName", sheet.getSheetName());
            response.put("columnCount", columnCount);
            response.put("rowCount", dataRowCount);
            response.put("message", "Az Excel fájl feldolgozása sikeres volt.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Hiba történt az Excel feldolgozása során: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private int countNonEmptyHeaderCells(Row headerRow) {
        int count = 0;
        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    count++;
                }
            }
        }

        return count;
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
                if (!cell.toString().trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}