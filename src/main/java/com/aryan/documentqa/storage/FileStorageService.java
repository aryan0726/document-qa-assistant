package com.aryan.documentqa.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(
            @Value("${app.storage.root:storage}") String storageRoot
    ) {
        this.storageRoot = Paths.get(storageRoot)
                .toAbsolutePath()
                .normalize();
    }

    public String store(
            String tenantId,
            UUID documentId,
            MultipartFile file
    ) throws IOException {

        Path tenantDirectory = storageRoot.resolve(tenantId);
        Path documentDirectory = tenantDirectory.resolve(
                documentId.toString()
        );

        Files.createDirectories(documentDirectory);

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        String safeFilename = Paths.get(originalFilename)
                .getFileName()
                .toString();

        Path targetPath = documentDirectory.resolve(safeFilename)
                .normalize();

        if (!targetPath.startsWith(documentDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        Files.copy(
                file.getInputStream(),
                targetPath
        );

        return targetPath.toString();
    }

    public Path resolve(String storagePath) {
        return Paths.get(storagePath)
                .toAbsolutePath()
                .normalize();
    }
}