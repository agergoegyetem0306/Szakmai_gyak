package hu.demo.backend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelReaderService {

    public void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Nincs feltöltött fájl.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Csak .xlsx Excel fájl támogatott.");
        }
    }

    public Row getHeaderRow(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("Az Excel fájl nem tartalmaz munkalapot.");
        }

        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new IllegalArgumentException("Az Excel fájl nem tartalmaz fejlécsort.");
        }

        return headerRow;
    }

    public void validateHasDataRows(Sheet sheet) {
        if (sheet == null || sheet.getLastRowNum() < 1) {
            throw new IllegalArgumentException("Az Excel fájl nem tartalmaz adatsort.");
        }
    }

    public List<String> readHeaderCells(Row headerRow) {
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

    public boolean isRowEmpty(Row row) {
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

    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("hu"));
        return formatter.formatCellValue(cell);
    }
}