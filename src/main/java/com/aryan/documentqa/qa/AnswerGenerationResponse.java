package com.aryan.documentqa.qa;

import com.aryan.documentqa.retrieval.RetrievedChunk;

import java.util.List;

public record AnswerGenerationResponse(
        String answer,
        List<RetrievedChunk> sources
) {
}