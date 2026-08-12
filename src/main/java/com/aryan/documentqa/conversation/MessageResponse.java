package com.aryan.documentqa.conversation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        MessageRole role,
        String content,
        Integer tokenCount,
        String model,
        Long latencyMs,
        OffsetDateTime createdAt
) {

    public static MessageResponse from(
            Message message
    ) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getRole(),
                message.getContent(),
                message.getTokenCount(),
                message.getModel(),
                message.getLatencyMs(),
                message.getCreatedAt()
        );
    }
}