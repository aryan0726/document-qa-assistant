package com.aryan.documentqa.qa;

import com.aryan.documentqa.retrieval.DocumentRetrievalService;
import com.aryan.documentqa.retrieval.RetrievedChunk;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerGenerationService {

    private final ChatClient chatClient;
    private final DocumentRetrievalService retrievalService;

    public AnswerGenerationService(
            ChatClient.Builder chatClientBuilder,
            DocumentRetrievalService retrievalService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.retrievalService = retrievalService;
    }

    public AnswerGenerationResponse answer(
            String tenantId,
            String question,
            int limit
    ) {

        validateTenantId(tenantId);
        validateQuestion(question);

        List<RetrievedChunk> chunks =
                retrievalService.retrieve(
                        tenantId,
                        question,
                        limit
                );

        if (chunks.isEmpty()) {
            return new AnswerGenerationResponse(
                    "I couldn't find relevant information in the provided documents.",
                    List.of()
            );
        }

        String context = buildContext(chunks);

        String prompt = buildPrompt(question, context);

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

        return new AnswerGenerationResponse(
                answer.trim(),
                chunks
        );
    }

    private String buildContext(List<RetrievedChunk> chunks) {

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
                """.formatted(context, question);
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