package hu.demo.backend.service;

import hu.demo.backend.model.CrosswalkValidationResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CrosswalkValidationService {

    private final CrosswalkService crosswalkService;
    private final ColumnClassificationService columnClassificationService;
    private final ExcelReaderService excelReaderService;

    public CrosswalkValidationService(
            CrosswalkService crosswalkService,
            ColumnClassificationService columnClassificationService,
            ExcelReaderService excelReaderService
    ) {
        this.crosswalkService = crosswalkService;
        this.columnClassificationService = columnClassificationService;
        this.excelReaderService = excelReaderService;
    }

    public CrosswalkValidationResult validate(MultipartFile file) {
        excelReaderService.validateExcelFile(file);

        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = excelReaderService.getHeaderRow(sheet);

            List<String> excelHeaders = excelReaderService.readHeaderCells(headerRow);
            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

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

            String message;

            if (unmappedColumns.isEmpty()) {
                message = "Minden metaadat-oszlop megfeleltethető.";
            } else {
                message = "Vannak olyan Excel oszlopok, amelyek nincsenek megfeleltetve.";
            }

            return new CrosswalkValidationResult(
                    fileName,
                    sheet.getSheetName(),
                    excelHeaders,
                    mappedColumns,
                    unmappedColumns,
                    technicalColumns,
                    bundleColumns,
                    crosswalk.size(),
                    message
            );

        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a crosswalk validáció során: " + e.getMessage(), e);
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
}