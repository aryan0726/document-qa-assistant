package com.aryan.documentqa.retrieval;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retrieval")
public class DocumentRetrievalController {

    private final DocumentRetrievalService retrievalService;

    public DocumentRetrievalController(
            DocumentRetrievalService retrievalService
    ) {
        this.retrievalService = retrievalService;
    }

    @GetMapping("/search")
    public List<RetrievedChunk> search(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return retrievalService.retrieve(
                tenantId,
                query,
                limit
        );
    }
}