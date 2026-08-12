package com.aryan.documentqa.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository
        extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(
            UUID conversationId
    );

    long countByConversationId(UUID conversationId);

    void deleteAllByConversationId(UUID conversationId);
}