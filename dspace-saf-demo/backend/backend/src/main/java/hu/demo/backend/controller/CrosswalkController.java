package hu.demo.backend.controller;

import hu.demo.backend.model.CrosswalkValidationResult;
import hu.demo.backend.service.CrosswalkValidationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crosswalk")
public class CrosswalkController {

    private final CrosswalkValidationService crosswalkValidationService;

    public CrosswalkController(CrosswalkValidationService crosswalkValidationService) {
        this.crosswalkValidationService = crosswalkValidationService;
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateCrosswalk(@RequestParam("file") MultipartFile file) {
        try {
            CrosswalkValidationResult result = crosswalkValidationService.validate(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}