package com.aryan.documentqa.document;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_documents_tenant_content_hash",
                        columnNames = {"tenant_id", "content_hash"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_documents_tenant_created_at",
                        columnList = "tenant_id, created_at"
                ),
                @Index(
                        name = "idx_documents_tenant_category",
                        columnList = "tenant_id, category"
                ),
                @Index(
                        name = "idx_documents_status",
                        columnList = "status"
                )
        }
)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 500)
    private String filename;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "storage_path", length = 1000)
    private String storagePath;

    protected Document() {
    }

    public Document(
            String tenantId,
            String title,
            String category,
            String filename,
            String contentHash,
            Long sizeBytes
    ) {
        this.tenantId = tenantId;
        this.title = title;
        this.category = category;
        this.filename = filename;
        this.contentHash = contentHash;
        this.sizeBytes = sizeBytes;
        this.status = DocumentStatus.PROCESSING;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void markReady() {
        this.status = DocumentStatus.READY;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = DocumentStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}