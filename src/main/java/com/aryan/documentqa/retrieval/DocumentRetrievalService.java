package com.aryan.documentqa.retrieval;

import com.aryan.documentqa.document.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;

    @Value("${app.retrieval.similarity-threshold:0.70}")
    private double similarityThreshold;

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
            String category,
            int limit
    ) {

        validateTenantId(tenantId);
        validateQuery(query);
        validateCategory(category);

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 50"
            );
        }

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
                        category,
                        similarityThreshold,
                        limit
                );

        return rows.stream()
                .map(this::mapToRetrievedChunk)
                .toList();
    }

    private RetrievedChunk mapToRetrievedChunk(Object[] row) {

        return new RetrievedChunk(
                (UUID) row[0],                         // id
                (UUID) row[1],                         // documentId
                (String) row[2],                       // tenantId
                (String) row[3],                       // documentTitle
                ((Number) row[4]).intValue(),          // chunkIndex
                (String) row[5],                       // content
                row[6] == null
                        ? null
                        : ((Number) row[6]).intValue(), // pageNumber
                row[7] == null
                        ? null
                        : ((Number) row[7]).intValue(), // tokenCount
                ((Number) row[8]).doubleValue()        // similarityScore
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

    private void validateCategory(String category) {

        if (category != null && category.length() > 100) {
            throw new IllegalArgumentException(
                    "Category must not exceed 100 characters"
            );
        }
    }
}