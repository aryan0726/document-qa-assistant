package com.aryan.documentqa.ingestion;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentTextExtractor {

    private final Tika tika = new Tika();

    public ExtractedDocument extract(Path file) throws IOException {

        if (file == null || !Files.exists(file)) {
            throw new IllegalArgumentException(
                    "Document file does not exist"
            );
        }

        String contentType = detectContentType(file);

        return switch (contentType) {

            case "application/pdf" ->
                    extractPdf(file, contentType);

            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    extractDocx(file, contentType);

            case "text/plain" ->
                    extractPlainText(file, contentType);

            case "text/markdown" ->
                    extractPlainText(file, contentType);

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported document type: " + contentType
                    );
        };
    }

    private String detectContentType(Path file) throws IOException {

        try (InputStream inputStream = Files.newInputStream(file)) {
            return tika.detect(
                    inputStream,
                    file.getFileName().toString()
            );
        }
    }

    /**
     * Extract PDF text page by page.
     *
     * Each PDF page becomes one ExtractedPage so that
     * later chunks can retain the correct source page number.
     */
    private ExtractedDocument extractPdf(
            Path file,
            String contentType
    ) throws IOException {

        List<ExtractedPage> pages = new ArrayList<>();

        try (PDDocument document =
                     Loader.loadPDF(file.toFile())) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            int totalPages = document.getNumberOfPages();

            for (int pageNumber = 1;
                 pageNumber <= totalPages;
                 pageNumber++) {

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String text = stripper
                        .getText(document)
                        .trim();

                pages.add(
                        new ExtractedPage(
                                pageNumber,
                                text
                        )
                );
            }
        }

        return new ExtractedDocument(
                contentType,
                pages
        );
    }

    /**
     * DOCX does not provide reliable physical page boundaries
     * through basic document parsing.
     *
     * Therefore the extracted DOCX content is represented as
     * page 1 for now.
     */
    private ExtractedDocument extractDocx(
            Path file,
            String contentType
    ) throws IOException {

        String text = extractWithTika(file);

        return new ExtractedDocument(
                contentType,
                List.of(
                        new ExtractedPage(
                                1,
                                text
                        )
                )
        );
    }

    /**
     * TXT and Markdown files are treated as single-page
     * documents.
     */
    private ExtractedDocument extractPlainText(
            Path file,
            String contentType
    ) throws IOException {

        String text = Files.readString(file);

        return new ExtractedDocument(
                contentType,
                List.of(
                        new ExtractedPage(
                                1,
                                text
                        )
                )
        );
    }

    /**
     * Generic Apache Tika extraction used for DOCX.
     */
    private String extractWithTika(Path file) throws IOException {

        try (InputStream inputStream =
                     Files.newInputStream(file)) {

            Metadata metadata = new Metadata();

            BodyContentHandler handler =
                    new BodyContentHandler(-1);

            AutoDetectParser parser =
                    new AutoDetectParser();

            try {

                parser.parse(
                        inputStream,
                        handler,
                        metadata
                );

            } catch (SAXException | TikaException e) {

                throw new IOException(
                        "Failed to extract document text",
                        e
                );
            }

            return handler
                    .toString()
                    .trim();
        }
    }
}