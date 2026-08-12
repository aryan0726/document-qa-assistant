package com.aryan.documentqa.ingestion;

import java.util.List;

public record ExtractedDocument(
        String contentType,
        List<ExtractedPage> pages
) {
}