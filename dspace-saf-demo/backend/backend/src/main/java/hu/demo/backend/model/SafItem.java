package hu.demo.backend.model;

import java.util.List;

public record SafItem(
        String itemName,
        int sourceRowNumber,
        String folderPath,
        String dublinCoreXml,
        String contents,
        List<BundleFile> bundleFiles
) {
}