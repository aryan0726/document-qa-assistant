package com.aryan.documentqa.retrieval;

import com.aryan.documentqa.document.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentRetrievalService(
            EmbeddingModel embeddingModel,
            DocumentChunkRepository documentChunkRepository
    ) {
        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
    }

    public List<RetrievedChunk> retrieve(
            String tenantId,
            String query,
            int limit
    ) {

        validateTenantId(tenantId);
        validateQuery(query);

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 50"
            );
        }

        /*
         * Convert the user's question into a Gemini embedding.
         *
         * The embedding model returns a 768-dimensional vector
         * for our configured Gemini embedding model.
         */
        float[] queryEmbedding = embeddingModel.embed(query);

        if (queryEmbedding == null) {
            throw new IllegalStateException(
                    "Query embedding generation returned null"
            );
        }

        if (queryEmbedding.length != 768) {
            throw new IllegalStateException(
                    "Expected 768-dimensional query embedding but received "
                            + queryEmbedding.length
            );
        }

        String vector = toPgVector(queryEmbedding);

        List<Object[]> rows =
                documentChunkRepository.findSimilarChunks(
                        tenantId,
                        vector,
                        limit
                );

        return rows.stream()
                .map(this::mapToRetrievedChunk)
                .toList();
    }

    private RetrievedChunk mapToRetrievedChunk(Object[] row) {

        return new RetrievedChunk(
                (java.util.UUID) row[0],
                (java.util.UUID) row[1],
                (String) row[2],
                ((Number) row[3]).intValue(),
                (String) row[4],
                row[5] == null
                        ? null
                        : ((Number) row[5]).intValue(),
                row[6] == null
                        ? null
                        : ((Number) row[6]).intValue(),
                ((Number) row[7]).doubleValue()
        );
    }

    private String toPgVector(float[] embedding) {

        StringBuilder vector = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                vector.append(",");
            }

            vector.append(embedding[i]);
        }

        vector.append("]");

        return vector.toString();
    }

    private void validateTenantId(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException(
                    "X-Tenant-Id is required"
            );
        }

        if (tenantId.length() > 100) {
            throw new IllegalArgumentException(
                    "X-Tenant-Id must not exceed 100 characters"
            );
        }
    }

    private void validateQuery(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Query is required"
            );
        }

        if (query.length() > 2000) {
            throw new IllegalArgumentException(
                    "Query must not exceed 2000 characters"
            );
        }
    }
}