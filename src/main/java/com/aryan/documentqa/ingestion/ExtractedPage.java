package com.aryan.documentqa.ingestion;

public record ExtractedPage(
        int pageNumber,
        String content
) {
}