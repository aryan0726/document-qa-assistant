package com.aryan.documentqa.qa;

import com.aryan.documentqa.conversation.Conversation;
import com.aryan.documentqa.conversation.ConversationRepository;
import com.aryan.documentqa.conversation.MessageRepository;
import com.aryan.documentqa.retrieval.DocumentRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerGenerationServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private DocumentRetrievalService retrievalService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    private AnswerGenerationService answerGenerationService;

    @BeforeEach
    void setUp() {

        when(chatClientBuilder.build())
                .thenReturn(chatClient);

        answerGenerationService =
                new AnswerGenerationService(
                        chatClientBuilder,
                        retrievalService,
                        conversationRepository,
                        messageRepository
                );
    }

    @Test
    void shouldRefuseWhenNoRelevantDocumentsAreFound() {

        String tenantId = "school-006";

        String question =
                "What is the school's football stadium capacity?";

        String category = null;

        Conversation conversation =
                new Conversation(
                        tenantId,
                        question
                );

        /*
         * The real service saves the newly created conversation.
         * Because the repository is mocked, return the same
         * conversation instead of null.
         */
        when(conversationRepository.save(any(Conversation.class)))
                .thenReturn(conversation);

        /*
         * Simulate retrieval finding no relevant chunks.
         */
        when(retrievalService.retrieve(
                tenantId,
                question,
                category,
                5
        )).thenReturn(List.of());

        AnswerGenerationResponse response =
                answerGenerationService.answer(
                        null,
                        tenantId,
                        question,
                        category,
                        5
                );

        /*
         * Verify the fixed refusal response.
         */
        assertEquals(
                "I couldn't find relevant information in the provided documents.",
                response.answer()
        );

        /*
         * Refusal must not contain sources.
         */
        assertTrue(
                response.sources().isEmpty()
        );

        /*
         * Verify retrieval was actually performed
         * with the category parameter.
         */
        verify(retrievalService).retrieve(
                tenantId,
                question,
                category,
                5
        );

        /*
         * Most important assertion:
         * the LLM must NOT be called when retrieval
         * returns no relevant chunks.
         */
        verify(chatClient, never()).prompt();
    }
}