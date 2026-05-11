package hu.demo.backend.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SafZipService {

    private final DublinCoreService dublinCoreService;

    public SafZipService(DublinCoreService dublinCoreService) {
        this.dublinCoreService = dublinCoreService;
    }

    public byte[] generateSafZip(MultipartFile file) {
        validateExcelFile(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null || sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("Az Excel fájl nem tartalmaz fejlécsort vagy adatsort.");
            }

            Map<String, Object> result = dublinCoreService.generateDublinCoreItems(file);

            Object itemsObject = result.get("items");

            if (!(itemsObject instanceof List<?> items)) {
                throw new IllegalStateException("Nem sikerült itemeket generálni a ZIP készítéshez.");
            }

            for (Object itemObject : items) {
                if (!(itemObject instanceof Map<?, ?> item)) {
                    continue;
                }

                Object itemNameObject = item.get("itemName");
                Object xmlObject = item.get("xml");

                if (itemNameObject == null || xmlObject == null) {
                    continue;
                }

                String itemName = itemNameObject.toString();
                String xml = xmlObject.toString();

                String entryName = itemName + "/dublin_core.xml";

                zipOut.putNextEntry(new ZipEntry(entryName));
                zipOut.write(xml.getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
            }

            zipOut.finish();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a SAF ZIP generálása során: " + e.getMessage(), e);
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
}