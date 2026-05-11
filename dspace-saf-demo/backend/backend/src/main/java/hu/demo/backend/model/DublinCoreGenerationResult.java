package hu.demo.backend.model;

import java.util.List;

public record DublinCoreGenerationResult(
        String fileName,
        String sheetName,
        List<String> excelHeaders,
        List<String> mappedColumns,
        List<String> unmappedColumns,
        List<String> technicalColumns,
        List<String> bundleColumns,
        int itemCount,
        List<DublinCoreItem> items,
        String message
) {
}