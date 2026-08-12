package com.aryan.documentqa.ingestion;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunker {

    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 150;

    public List<ChunkedText> chunk(
            ExtractedDocument document
    ) {

        List<ChunkedText> chunks = new ArrayList<>();

        for (ExtractedPage page : document.pages()) {

            String text = normalize(page.content());

            if (text.isBlank()) {
                continue;
            }

            chunks.addAll(
                    chunkPage(
                            text,
                            page.pageNumber()
                    )
            );
        }

        return chunks;
    }

    private List<ChunkedText> chunkPage(
            String text,
            int pageNumber
    ) {

        List<ChunkedText> chunks = new ArrayList<>();

        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {

            int end = Math.min(
                    start + DEFAULT_CHUNK_SIZE,
                    text.length()
            );

            if (end < text.length()) {

                int paragraphBreak =
                        text.lastIndexOf("\n\n", end);

                int sentenceBreak =
                        text.lastIndexOf(". ", end);

                int whitespaceBreak =
                        text.lastIndexOf(" ", end);

                if (paragraphBreak > start) {
                    end = paragraphBreak;
                } else if (sentenceBreak > start) {
                    end = sentenceBreak + 1;
                } else if (whitespaceBreak > start) {
                    end = whitespaceBreak;
                }
            }

            String chunk = text
                    .substring(start, end)
                    .trim();

            if (!chunk.isBlank()) {

                chunks.add(
                        new ChunkedText(
                                chunkIndex++,
                                pageNumber,
                                chunk
                        )
                );
            }

            if (end >= text.length()) {
                break;
            }

            start = Math.max(
                    end - DEFAULT_OVERLAP,
                    start + 1
            );
        }

        return chunks;
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}