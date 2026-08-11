package com.aryan.documentqa.document;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "document_chunks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_chunks_document_index",
                        columnNames = {"document_id", "chunk_index"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_document_chunks_tenant_document",
                        columnList = "tenant_id, document_id"
                ),
                @Index(
                        name = "idx_document_chunks_document_page",
                        columnList = "document_id, page_number, chunk_index"
                )
        }
)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(
            name = "embedding",
            columnDefinition = "vector(768)"
    )
    private float[] embedding;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected DocumentChunk() {
    }

    public DocumentChunk(
            UUID documentId,
            String tenantId,
            Integer chunkIndex,
            String content,
            Integer pageNumber,
            Integer tokenCount
    ) {
        this.documentId = documentId;
        this.tenantId = tenantId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.pageNumber = pageNumber;
        this.tokenCount = tokenCount;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}