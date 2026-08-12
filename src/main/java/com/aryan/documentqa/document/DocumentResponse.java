package com.aryan.documentqa.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String tenantId,
        String title,
        String category,
        String filename,
        Long sizeBytes,
        DocumentStatus status,
        String errorMessage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DocumentResponse from(
            Document document
    ) {
        return new DocumentResponse(
                document.getId(),
                document.getTenantId(),
                document.getTitle(),
                document.getCategory(),
                document.getFilename(),
                document.getSizeBytes(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}