package com.aryan.documentqa.qa;

import com.aryan.documentqa.conversation.Conversation;
import com.aryan.documentqa.conversation.ConversationRepository;
import com.aryan.documentqa.conversation.Message;
import com.aryan.documentqa.conversation.MessageRepository;
import com.aryan.documentqa.conversation.MessageRole;
import com.aryan.documentqa.retrieval.DocumentRetrievalService;
import com.aryan.documentqa.retrieval.RetrievedChunk;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AnswerGenerationService {

    private final ChatClient chatClient;
    private final DocumentRetrievalService retrievalService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public AnswerGenerationService(
            ChatClient.Builder chatClientBuilder,
            DocumentRetrievalService retrievalService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this.chatClient = chatClientBuilder.build();
        this.retrievalService = retrievalService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public AnswerGenerationResponse answer(
            UUID conversationId,
            String tenantId,
            String question,
            int limit
    ) {

        validateTenantId(tenantId);
        validateQuestion(question);

        long startTime = System.currentTimeMillis();

        Conversation conversation;

        if (conversationId == null) {

            String title = createConversationTitle(question);

            conversation = new Conversation(
                    tenantId,
                    title
            );

            conversation = conversationRepository.save(conversation);

        } else {

            conversation = conversationRepository
                    .findByIdAndTenantId(conversationId, tenantId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Conversation not found for this tenant"
                            )
                    );
        }

        Message userMessage = new Message(
                conversation,
                MessageRole.USER,
                question
        );

        messageRepository.save(userMessage);

        conversation.updateLastMessageAt();
        conversationRepository.save(conversation);

        List<RetrievedChunk> chunks =
                retrievalService.retrieve(
                        tenantId,
                        question,
                        limit
                );

        if (chunks.isEmpty()) {

            String fallbackAnswer =
                    "I couldn't find relevant information in the provided documents.";

            Message assistantMessage = new Message(
                    conversation,
                    MessageRole.ASSISTANT,
                    fallbackAnswer
            );

            assistantMessage.setLatencyMs(
                    System.currentTimeMillis() - startTime
            );

            messageRepository.save(assistantMessage);

            conversation.updateLastMessageAt();
            conversationRepository.save(conversation);

            return new AnswerGenerationResponse(
                    fallbackAnswer,
                    List.of()
            );
        }

        String context = buildContext(chunks);

        String prompt = buildPrompt(
                question,
                context
        );

        String answer = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException(
                    "The AI model returned an empty answer"
            );
        }

        answer = answer.trim();

        long latencyMs =
                System.currentTimeMillis() - startTime;

        Message assistantMessage = new Message(
                conversation,
                MessageRole.ASSISTANT,
                answer
        );

        assistantMessage.setLatencyMs(latencyMs);

        messageRepository.save(assistantMessage);

        conversation.updateLastMessageAt();
        conversationRepository.save(conversation);

        return new AnswerGenerationResponse(
                answer,
                chunks
        );
    }

    private String createConversationTitle(String question) {

        String title = question.trim();

        if (title.length() <= 255) {
            return title;
        }

        return title.substring(0, 252) + "...";
    }

    private String buildContext(
            List<RetrievedChunk> chunks
    ) {

        StringBuilder context = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {

            RetrievedChunk chunk = chunks.get(i);

            context.append("SOURCE ")
                    .append(i + 1)
                    .append("\n");

            context.append("Document ID: ")
                    .append(chunk.documentId())
                    .append("\n");

            context.append("Page: ")
                    .append(chunk.pageNumber())
                    .append("\n");

            context.append("Content:\n")
                    .append(chunk.content())
                    .append("\n\n");
        }

        return context.toString();
    }

    private String buildPrompt(
            String question,
            String context
    ) {

        return """
                You are a document question-answering assistant.

                Answer the user's question using ONLY the information
                provided in the sources below.

                Rules:
                1. Do not invent or assume information.
                2. If the answer cannot be found in the sources,
                   clearly say that the information is not available
                   in the provided documents.
                3. Give a concise and direct answer.
                4. Do not mention internal implementation details.
                5. Do not use outside knowledge.

                SOURCES:
                %s

                USER QUESTION:
                %s

                ANSWER:
                """.formatted(
                context,
                question
        );
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

    private void validateQuestion(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question is required"
            );
        }

        if (question.length() > 2000) {
            throw new IllegalArgumentException(
                    "Question must not exceed 2000 characters"
            );
        }
    }
}