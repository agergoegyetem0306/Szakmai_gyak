package hu.demo.backend.service;

import hu.demo.backend.model.SafItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SafZipService {

    private final SafPackageService safPackageService;

    public SafZipService(SafPackageService safPackageService) {
        this.safPackageService = safPackageService;
    }

    public byte[] generateSafZip(MultipartFile file) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            List<SafItem> safItems = safPackageService.generateSafItems(file);

            for (SafItem item : safItems) {
                addTextFileToZip(
                        zipOut,
                        item.itemName() + "/dublin_core.xml",
                        item.dublinCoreXml()
                );

                addTextFileToZip(
                        zipOut,
                        item.itemName() + "/contents",
                        item.contents()
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