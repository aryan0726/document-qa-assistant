package com.aryan.documentqa.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByTenantIdAndContentHash(
            String tenantId,
            String contentHash
    );

    List<Document> findAllByTenantIdOrderByCreatedAtDesc(
            String tenantId
    );

    Optional<Document> findByIdAndTenantId(
            UUID id,
            String tenantId
    );
}