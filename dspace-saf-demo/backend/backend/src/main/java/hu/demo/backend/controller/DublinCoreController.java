package hu.demo.backend.controller;

import hu.demo.backend.model.DublinCoreGenerationResult;
import hu.demo.backend.service.DublinCoreService;
import hu.demo.backend.service.SafZipService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dublin-core")
public class DublinCoreController {

    private final DublinCoreService dublinCoreService;
    private final SafZipService safZipService;

    public DublinCoreController(
            DublinCoreService dublinCoreService,
            SafZipService safZipService
    ) {
        this.dublinCoreService = dublinCoreService;
        this.safZipService = safZipService;
    }

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateDublinCore(@RequestParam("file") MultipartFile file) {
        try {
            DublinCoreGenerationResult result = dublinCoreService.generateDublinCoreItems(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping(value = "/generate-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateSafZip(@RequestParam("file") MultipartFile file) {
        try {
            byte[] zipBytes = safZipService.generateSafZip(file);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"saf_export.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}