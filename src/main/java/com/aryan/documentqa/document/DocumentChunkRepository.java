package com.aryan.documentqa.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

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
                        id,
                        document_id,
                        tenant_id,
                        chunk_index,
                        content,
                        page_number,
                        token_count,
                        1 - (embedding <=> CAST(:embedding AS vector)) AS similarity_score
                    FROM document_chunks
                    WHERE tenant_id = :tenantId
                      AND embedding IS NOT NULL
                    ORDER BY embedding <=> CAST(:embedding AS vector)
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<Object[]> findSimilarChunks(
            @Param("tenantId") String tenantId,
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );
}