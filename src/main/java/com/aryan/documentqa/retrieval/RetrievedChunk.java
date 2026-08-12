package com.aryan.documentqa.retrieval;

import java.util.UUID;

public record RetrievedChunk(
        UUID id,
        UUID documentId,
        String tenantId,
        String documentTitle,
        Integer chunkIndex,
        String content,
        Integer pageNumber,
        Integer tokenCount,
        double similarityScore
) {
}