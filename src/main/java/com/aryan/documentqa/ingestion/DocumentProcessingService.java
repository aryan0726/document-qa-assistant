package com.aryan.documentqa.ingestion;

import com.aryan.documentqa.document.Document;
import com.aryan.documentqa.document.DocumentChunk;
import com.aryan.documentqa.document.DocumentChunkRepository;
import com.aryan.documentqa.document.DocumentRepository;
import com.aryan.documentqa.document.DocumentStatus;
import com.aryan.documentqa.storage.FileStorageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final FileStorageService fileStorageService;
    private final DocumentTextExtractor documentTextExtractor;
    private final DocumentChunker documentChunker;
    private final DocumentEmbeddingService documentEmbeddingService;

    public DocumentProcessingService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            FileStorageService fileStorageService,
            DocumentTextExtractor documentTextExtractor,
            DocumentChunker documentChunker,
            DocumentEmbeddingService documentEmbeddingService
    ) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.fileStorageService = fileStorageService;
        this.documentTextExtractor = documentTextExtractor;
        this.documentChunker = documentChunker;
        this.documentEmbeddingService = documentEmbeddingService;
    }

    @Async
    @Transactional
    public void process(UUID documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + documentId
                ));

        try {
            document.setStatus(DocumentStatus.PROCESSING);
            document.setErrorMessage(null);
            documentRepository.save(document);

            if (document.getStoragePath() == null
                    || document.getStoragePath().isBlank()) {

                throw new IllegalStateException(
                        "Document storage path is missing"
                );
            }

            Path filePath = fileStorageService.resolve(
                    document.getStoragePath()
            );

            if (!Files.exists(filePath)) {
                throw new IllegalStateException(
                        "Stored document file does not exist: "
                                + filePath
                );
            }

            ExtractedDocument extractedDocument =
                    documentTextExtractor.extract(filePath);

            if (extractedDocument.pages() == null
                    || extractedDocument.pages().isEmpty()) {

                throw new IllegalStateException(
                        "No text could be extracted from document"
                );
            }

            List<ChunkedText> chunks =
                    documentChunker.chunk(extractedDocument);

            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No chunks were created from document"
                );
            }

            /*
             * Remove any existing chunks for this document.
             *
             * This makes processing idempotent. If a document is
             * processed again, we don't create duplicate chunks.
             */
            documentChunkRepository.deleteAllByDocumentId(
                    document.getId()
            );

            List<DocumentChunk> documentChunks =
                    new ArrayList<>();

            for (ChunkedText chunk : chunks) {

                int tokenCount = estimateTokenCount(
                        chunk.content()
                );

                DocumentChunk documentChunk =
                        new DocumentChunk(
                                document.getId(),
                                document.getTenantId(),
                                chunk.chunkIndex(),
                                chunk.content(),
                                chunk.pageNumber(),
                                tokenCount
                        );

                documentChunks.add(documentChunk);
            }

            documentChunkRepository.saveAll(documentChunks);
            documentEmbeddingService.embedChunks(documentChunks);

            /*
             * At this stage the document has been successfully
             * extracted and chunked.
             *
             * Embeddings will be generated in the next milestone.
             */
            document.markReady();

            documentRepository.save(document);

        } catch (Exception exception) {

            document.markFailed(
                    buildErrorMessage(exception)
            );

            documentRepository.save(document);

            throw new IllegalStateException(
                    "Failed to process document: "
                            + documentId,
                    exception
            );
        }
    }

    /**
     * This is only an approximate token count.
     *
     * We will replace this with the actual tokenizer/
     * embedding-model token count later if required.
     */
    private int estimateTokenCount(String content) {

        if (content == null || content.isBlank()) {
            return 0;
        }

        return content.trim()
                .split("\\s+")
                .length;
    }

    private String buildErrorMessage(Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message.length() > 2000
                ? message.substring(0, 2000)
                : message;
    }
}