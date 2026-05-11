package hu.demo.backend.model;

import java.util.List;

public record CrosswalkValidationResult(
        String fileName,
        String sheetName,
        List<String> excelHeaders,
        List<String> mappedColumns,
        List<String> unmappedColumns,
        List<String> technicalColumns,
        List<String> bundleColumns,
        int crosswalkSize,
        String message
) {
}