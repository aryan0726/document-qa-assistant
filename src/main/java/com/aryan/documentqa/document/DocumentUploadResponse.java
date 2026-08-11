package com.aryan.documentqa.document;

import java.util.UUID;

public record DocumentUploadResponse(
        UUID id,
        String tenantId,
        String title,
        String category,
        String filename,
        Long sizeBytes,
        String status
) {
}