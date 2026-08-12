package com.aryan.documentqa.conversation;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(
                        name = "idx_conversations_tenant_last_message",
                        columnList = "tenant_id, last_message_at"
                )
        }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "tenant_id",
            nullable = false,
            length = 100
    )
    private String tenantId;

    @Column(length = 255)
    private String title;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "last_message_at",
            nullable = false
    )
    private OffsetDateTime lastMessageAt;

    protected Conversation() {
    }

    public Conversation(
            String tenantId,
            String title
    ) {
        this.tenantId = tenantId;
        this.title = title;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        this.createdAt = now;
        this.lastMessageAt = now;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = OffsetDateTime.now();
    }

    public void setTitle(String title) {
        this.title = title;
    }
}