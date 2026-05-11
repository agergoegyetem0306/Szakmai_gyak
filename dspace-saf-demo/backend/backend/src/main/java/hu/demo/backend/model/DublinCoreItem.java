package hu.demo.backend.model;

public record DublinCoreItem(
        String itemName,
        int sourceRowNumber,
        String folderPath,
        String xml
) {
}