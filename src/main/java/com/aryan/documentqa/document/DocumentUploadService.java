package com.aryan.documentqa.document;

import com.aryan.documentqa.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.aryan.documentqa.ingestion.DocumentProcessingService;
@Service
public class DocumentUploadService {

    private final DocumentRepository documentRepository;
    private final DocumentProcessingService documentProcessingService;
    private final FileStorageService fileStorageService;

    public DocumentUploadService(
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            DocumentProcessingService documentProcessingService
    ) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.documentProcessingService = documentProcessingService;
    }

    public DocumentUploadResponse upload(
            String tenantId,
            String title,
            String category,
            MultipartFile file
    ) throws IOException {

        validateTenantId(tenantId);
        validateTitle(title);
        validateFile(file);

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        byte[] fileBytes = file.getBytes();

        String contentHash = calculateSha256(fileBytes);

        if (documentRepository
                .findByTenantIdAndContentHash(tenantId, contentHash)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A document with the same content already exists for this tenant"
            );
        }

        Document document = new Document(
                tenantId,
                title,
                category,
                filename,
                contentHash,
                file.getSize()
        );

        /*
         * Save first so that the document receives its UUID.
         */
        Document savedDocument = documentRepository.save(document);

        /*
         * Store the original uploaded file using:
         *
         * storage/{tenantId}/{documentId}/{filename}
         */
        String storagePath = fileStorageService.store(
                tenantId,
                savedDocument.getId(),
                file
        );

        /*
         * Save the generated storage path in the database.
         */
        savedDocument.setStoragePath(storagePath);

        savedDocument = documentRepository.save(savedDocument);

        documentProcessingService.process(
                savedDocument.getId()
        );

        return new DocumentUploadResponse(
                savedDocument.getId(),
                savedDocument.getTenantId(),
                savedDocument.getTitle(),
                savedDocument.getCategory(),
                savedDocument.getFilename(),
                savedDocument.getSizeBytes(),
                savedDocument.getStatus().name()
        );
    }

    private void validateTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("X-Tenant-Id is required");
        }

        if (tenantId.length() > 100) {
            throw new IllegalArgumentException(
                    "X-Tenant-Id must not exceed 100 characters"
            );
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (title.length() > 255) {
            throw new IllegalArgumentException(
                    "Title must not exceed 255 characters"
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document file is required"
            );
        }
    }

    private String calculateSha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(content);

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}