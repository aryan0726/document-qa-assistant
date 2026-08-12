package com.aryan.documentqa.qa;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qa")
public class QuestionAnswerController {

    private final AnswerGenerationService answerGenerationService;

    public QuestionAnswerController(
            AnswerGenerationService answerGenerationService
    ) {
        this.answerGenerationService = answerGenerationService;
    }

    @PostMapping("/ask")
    public AnswerGenerationResponse ask(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody QuestionRequest request
    ) {
        return answerGenerationService.answer(
                tenantId,
                request.question(),
                5
        );
    }
}