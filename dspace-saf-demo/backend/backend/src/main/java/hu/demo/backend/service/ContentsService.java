package hu.demo.backend.service;

import hu.demo.backend.model.BundleFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContentsService {

    private static final String FILE_SEPARATOR = ";";

    private final ColumnClassificationService columnClassificationService;
    private final ExcelReaderService excelReaderService;

    public ContentsService(
            ColumnClassificationService columnClassificationService,
            ExcelReaderService excelReaderService
    ) {
        this.columnClassificationService = columnClassificationService;
        this.excelReaderService = excelReaderService;
    }

    public List<BundleFile> extractBundleFiles(Row headerRow, Row dataRow) {
        List<BundleFile> bundleFiles = new ArrayList<>();

        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            Cell headerCell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (headerCell == null) {
                continue;
            }

            String header = excelReaderService.getCellValueAsString(headerCell).trim();

            if (!columnClassificationService.isBundleColumn(header)) {
                continue;
            }

            Cell valueCell = dataRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            if (valueCell == null) {
                continue;
            }

            String cellValue = excelReaderService.getCellValueAsString(valueCell).trim();

            if (cellValue.isEmpty()) {
                continue;
            }

            List<String> filePaths = splitFilePaths(cellValue);

            for (String filePath : filePaths) {
                String fileName = extractFileName(filePath);

                if (fileName.isEmpty()) {
                    continue;
                }

                BundleFile bundleFile = new BundleFile(
                        fileName,
                        header,
                        "",
                        defaultDescriptionForBundle(header)
                );

                bundleFiles.add(bundleFile);
            }
        }

        return bundleFiles;
    }

    public String generateContents(List<BundleFile> bundleFiles) {
        StringBuilder contents = new StringBuilder();

        for (BundleFile file : bundleFiles) {
            contents.append(file.fileName())
                    .append(" bundle:")
                    .append(file.bundle());

            if (file.permissions() != null && !file.permissions().isBlank()) {
                contents.append(" permissions:")
                        .append(file.permissions());
            }

            if (file.description() != null && !file.description().isBlank()) {
                contents.append(" description:")
                        .append(file.description());
            }

            contents.append("\n");
        }

        return contents.toString();
    }

    private List<String> splitFilePaths(String cellValue) {
        List<String> result = new ArrayList<>();

        String[] parts = cellValue.split(FILE_SEPARATOR);

        for (String part : parts) {
            String trimmed = part.trim();

            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        String normalized = path.trim()
                .replace("\\", "/");

        return Paths.get(normalized).getFileName().toString();
    }

    private String defaultDescriptionForBundle(String bundle) {
        if ("TEXT".equals(bundle)) {
            return "Extracted text";
        }

        return "";
    }
}