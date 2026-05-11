package hu.demo.backend.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ColumnClassificationService {

    private static final Set<String> TECHNICAL_COLUMNS = Set.of(
            "Mappa"
    );

    private static final Set<String> BUNDLE_COLUMNS = Set.of(
            "ORIGINAL",
            "TEXT",
            "PRESERVATION",
            "THUMBNAIL"
    );

    public boolean isTechnicalColumn(String columnName) {
        return columnName != null && TECHNICAL_COLUMNS.contains(columnName.trim());
    }

    public boolean isBundleColumn(String columnName) {
        return columnName != null && BUNDLE_COLUMNS.contains(columnName.trim());
    }

    public boolean isSystemColumn(String columnName) {
        return isTechnicalColumn(columnName) || isBundleColumn(columnName);
    }

    public Set<String> getTechnicalColumns() {
        return TECHNICAL_COLUMNS;
    }

    public Set<String> getBundleColumns() {
        return BUNDLE_COLUMNS;
    }
}