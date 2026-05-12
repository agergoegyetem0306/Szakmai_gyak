package hu.demo.backend.model;

public record BundleFile(
        String fileName,
        String bundle,
        String permissions,
        String description
) {
}