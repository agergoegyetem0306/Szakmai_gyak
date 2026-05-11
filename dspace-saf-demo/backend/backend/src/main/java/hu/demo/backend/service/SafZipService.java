package hu.demo.backend.service;

import hu.demo.backend.model.DublinCoreGenerationResult;
import hu.demo.backend.model.DublinCoreItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SafZipService {

    private final DublinCoreService dublinCoreService;

    public SafZipService(DublinCoreService dublinCoreService) {
        this.dublinCoreService = dublinCoreService;
    }

    public byte[] generateSafZip(MultipartFile file) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            DublinCoreGenerationResult dublinCoreResult = dublinCoreService.generateDublinCoreItems(file);

            for (DublinCoreItem item : dublinCoreResult.items()) {
                addTextFileToZip(
                        zipOut,
                        item.itemName() + "/dublin_core.xml",
                        item.xml()
                );
            }

            zipOut.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Hiba történt a SAF ZIP generálása során: " + e.getMessage(), e);
        }
    }

    private void addTextFileToZip(ZipOutputStream zipOut, String entryName, String content) throws Exception {
        zipOut.putNextEntry(new ZipEntry(entryName));
        zipOut.write(content.getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();
    }
}