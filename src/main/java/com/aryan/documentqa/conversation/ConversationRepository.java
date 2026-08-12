package com.aryan.documentqa.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    List<Conversation> findAllByTenantIdOrderByLastMessageAtDesc(
            String tenantId
    );

    Optional<Conversation> findByIdAndTenantId(
            UUID id,
            String tenantId
    );
}