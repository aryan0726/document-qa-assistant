package com.aryan.documentqa.qa;

import org.springframework.web.bind.annotation.*;

@RestController
public class QuestionAnswerController {

    private final AnswerGenerationService answerGenerationService;

    public QuestionAnswerController(
            AnswerGenerationService answerGenerationService
    ) {
        this.answerGenerationService = answerGenerationService;
    }

    @PostMapping("/api/v1/qa/ask")
    public AnswerGenerationResponse ask(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody QuestionRequest request
    ) {

        return answerGenerationService.answer(
                request.conversationId(),
                tenantId,
                request.question(),
                request.category(),
                5
        );
    }

    @PostMapping("/api/v1/chat")
    public AnswerGenerationResponse chat(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody QuestionRequest request
    ) {

        return answerGenerationService.answer(
                request.conversationId(),
                tenantId,
                request.question(),
                request.category(),
                5
        );
    }
}