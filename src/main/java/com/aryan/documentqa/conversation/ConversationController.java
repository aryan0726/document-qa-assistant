package com.aryan.documentqa.conversation;

import com.aryan.documentqa.common.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationController(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public List<ConversationResponse> listConversations(
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {

        validateTenantId(tenantId);

        return conversationRepository
                .findAllByTenantIdOrderByLastMessageAtDesc(tenantId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> listMessages(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable UUID conversationId
    ) {

        validateTenantId(tenantId);

        conversationRepository
                .findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found"
                        )
                );

        return messageRepository
                .findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    private void validateTenantId(String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException(
                    "X-Tenant-Id is required"
            );
        }

        if (tenantId.length() > 100) {
            throw new IllegalArgumentException(
                    "X-Tenant-Id must not exceed 100 characters"
            );
        }
    }
}