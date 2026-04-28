package hu.demo.backend.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CrosswalkService {

    public Map<String, String> loadCrosswalk() {
        Map<String, String> crosswalk = new LinkedHashMap<>();

        try {
            ClassPathResource resource = new ClassPathResource("crosswalk.txt");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split("\\t");

                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Hibás crosswalk sor: " + line);
                    }

                    String excelColumn = parts[0].trim();
                    String dublinCoreField = parts[1].trim();

                    if (!excelColumn.isEmpty() && !dublinCoreField.isEmpty()) {
                        crosswalk.put(excelColumn, dublinCoreField);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Nem sikerült beolvasni a crosswalk.txt fájlt.", e);
        }

        return crosswalk;
    }
}