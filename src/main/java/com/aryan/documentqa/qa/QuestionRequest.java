package com.aryan.documentqa.qa;

import java.util.UUID;

public record QuestionRequest(
        UUID conversationId,
        String question,
        String category
) {
}