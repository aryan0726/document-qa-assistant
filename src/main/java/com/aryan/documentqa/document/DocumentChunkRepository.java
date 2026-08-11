package com.aryan.documentqa.document;

import org.springframework.data.jpa.repository.JpaRepository;

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
}