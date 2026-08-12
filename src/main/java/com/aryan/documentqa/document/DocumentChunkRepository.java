package com.aryan.documentqa.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(
            UUID documentId
    );

    List<DocumentChunk> findAllByTenantIdAndDocumentIdOrderByChunkIndexAsc(
            String tenantId,
            UUID documentId
    );

    long countByDocumentId(UUID documentId);

    void deleteAllByDocumentId(UUID documentId);

    @Query(
            value = """
                    SELECT
                        dc.id,
                        dc.document_id,
                        dc.tenant_id,
                        d.title,
                        dc.chunk_index,
                        dc.content,
                        dc.page_number,
                        dc.token_count,
                        1 - (dc.embedding <=> CAST(:embedding AS vector)) AS similarity_score
                    FROM document_chunks dc
                    JOIN documents d
                      ON d.id = dc.document_id
                    WHERE dc.tenant_id = :tenantId
                      AND d.tenant_id = :tenantId
                      AND dc.embedding IS NOT NULL
                      AND (:category IS NULL OR d.category = :category)
                      AND 1 - (dc.embedding <=> CAST(:embedding AS vector)) >= :threshold
                    ORDER BY dc.embedding <=> CAST(:embedding AS vector)
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<Object[]> findSimilarChunks(
            @Param("tenantId") String tenantId,
            @Param("embedding") String embedding,
            @Param("category") String category,
            @Param("threshold") double threshold,
            @Param("limit") int limit
    );
}