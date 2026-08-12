package com.aryan.documentqa.ingestion;

public record ChunkedText(
        int chunkIndex,
        int pageNumber,
        String content
) {
}