package hu.demo.backend.service;

import hu.demo.backend.model.BundleFile;
import hu.demo.backend.model.SafItem;
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
public class SafPackageService {

    private final ExcelReaderService excelReaderService;
    private final CrosswalkService crosswalkService;
    private final DublinCoreService dublinCoreService;
    private final ContentsService contentsService;

    public SafPackageService(
            ExcelReaderService excelReaderService,
            CrosswalkService crosswalkService,
            DublinCoreService dublinCoreService,
            ContentsService contentsService
    ) {
        this.excelReaderService = excelReaderService;
        this.crosswalkService = crosswalkService;
        this.dublinCoreService = dublinCoreService;
        this.contentsService = contentsService;
    }

    public List<SafItem> generateSafItems(MultipartFile file) {
        excelReaderService.validateExcelFile(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = excelReaderService.getHeaderRow(sheet);
            excelReaderService.validateHasDataRows(sheet);

            Map<String, String> crosswalk = crosswalkService.loadCrosswalk();

            List<SafItem> safItems = new ArrayList<>();
            int itemIndex = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);

                if (excelReaderService.isRowEmpty(dataRow)) {
                    continue;
                }

                String itemName = String.format("item_%03d", itemIndex);
                String folderPath = findTechnicalValue(headerRow, dataRow, "Mappa");

                String dublinCoreXml = dublinCoreService.generateXmlForRow(
                        headerRow,
                        dataRow,
                        crosswalk
                );

                List<BundleFile> bundleFiles = contentsService.extractBundleFiles(
                        headerRow,
                        dataRow
                );

                String contents = contentsService.generateContents(bundleFiles);

                SafItem safItem = new SafItem(
                        itemName,
                        rowIndex + 1,
                        folderPath,
                        dublinCoreXml,
                        contents,
                        bundleFiles
                );

                safItems.add(safItem);
                itemIndex++;
            }

            return safItems;

        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a SAF itemek generálása során: " + e.getMessage(), e);
        }
    }

    private String findTechnicalValue(Row headerRow, Row dataRow, String columnName) {
        short lastCellNum = headerRow.getLastCellNum();

        for (int i = 0; i < lastCellNum; i++) {
            String header = excelReaderService.getCellValueAsString(
                    headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            ).trim();

            if (!columnName.equals(header)) {
                continue;
            }

            String value = excelReaderService.getCellValueAsString(
                    dataRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            ).trim();

            return normalizePath(value);
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
}