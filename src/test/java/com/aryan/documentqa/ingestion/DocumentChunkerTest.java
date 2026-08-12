package com.aryan.documentqa.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkerTest {

    private final DocumentChunker chunker = new DocumentChunker();

    @Test
    void shouldCreateChunksAndPreservePageNumbers() {

        String pageOneText = """
                This is page one of the document.
                It contains information about school rules.
                Students must carry their identification cards.
                """;

        String pageTwoText = """
                This is page two of the document.
                The library is open from 8 AM to 5 PM.
                Students must maintain silence inside the library.
                """;

        ExtractedDocument document = new ExtractedDocument(
                "application/pdf",
                List.of(
                        new ExtractedPage(1, pageOneText),
                        new ExtractedPage(2, pageTwoText)
                )
        );

        List<ChunkedText> chunks = chunker.chunk(document);

        assertFalse(chunks.isEmpty());

        assertTrue(
                chunks.stream()
                        .anyMatch(chunk -> chunk.pageNumber() == 1)
        );

        assertTrue(
                chunks.stream()
                        .anyMatch(chunk -> chunk.pageNumber() == 2)
        );

        assertTrue(
                chunks.stream()
                        .allMatch(chunk ->
                                chunk.content() != null
                                        && !chunk.content().isBlank()
                        )
        );
    }

    @Test
    void shouldIgnoreEmptyPages() {

        ExtractedDocument document = new ExtractedDocument(
                "application/pdf",
                List.of(
                        new ExtractedPage(1, ""),
                        new ExtractedPage(2, "Actual document content.")
                )
        );

        List<ChunkedText> chunks = chunker.chunk(document);

        assertFalse(chunks.isEmpty());

        assertTrue(
                chunks.stream()
                        .noneMatch(chunk -> chunk.pageNumber() == 1)
        );

        assertTrue(
                chunks.stream()
                        .anyMatch(chunk -> chunk.pageNumber() == 2)
        );
    }
}