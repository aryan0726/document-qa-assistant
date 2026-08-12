package com.aryan.documentqa.conversation;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(
                        name = "idx_messages_conversation_created",
                        columnList = "conversation_id, created_at"
                )
        }
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false
    )
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private MessageRole role;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(length = 150)
    private String model;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    protected Message() {
    }

    public Message(
            Conversation conversation,
            MessageRole role,
            String content
    ) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public String getModel() {
        return model;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }
}