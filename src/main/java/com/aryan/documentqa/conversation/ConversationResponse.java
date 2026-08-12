package com.aryan.documentqa.conversation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String tenantId,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime lastMessageAt
) {

    public static ConversationResponse from(
            Conversation conversation
    ) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTenantId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getLastMessageAt()
        );
    }
}