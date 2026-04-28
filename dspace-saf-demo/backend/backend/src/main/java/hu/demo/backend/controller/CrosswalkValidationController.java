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
@RequestMapping("/api/crosswalk")
public class CrosswalkValidationController {

    private final CrosswalkService crosswalkService;

    public CrosswalkValidationController(CrosswalkService crosswalkService) {
        this.crosswalkService = crosswalkService;
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> validateCrosswalk(@RequestParam("file") MultipartFile file) {
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

            List<String> excelHeaders = readHeaderCells(headerRow);
            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            List<String> mappedColumns = new ArrayList<>();
            List<String> unmappedColumns = new ArrayList<>();

            for (String header : excelHeaders) {
                if (crosswalk.containsKey(header)) {
                    mappedColumns.add(header);
                } else {
                    unmappedColumns.add(header);
                }
            }

            response.put("fileName", fileName);
            response.put("sheetName", sheet.getSheetName());
            response.put("excelHeaders", excelHeaders);
            response.put("mappedColumns", mappedColumns);
            response.put("unmappedColumns", unmappedColumns);
            response.put("crosswalkSize", crosswalk.size());

            if (unmappedColumns.isEmpty()) {
                response.put("message", "Minden Excel oszlop szerepel a crosswalk megfeleltetésben.");
            } else {
                response.put("message", "Vannak olyan Excel oszlopok, amelyek nincsenek megfeleltetve.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Hiba történt a crosswalk validáció során: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private List<String> readHeaderCells(Row headerRow) {
        List<String> headers = new ArrayList<>();

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();

                if (!value.isEmpty()) {
                    headers.add(value);
                }
            }
        }

        return headers;
    }
}