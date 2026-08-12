package com.aryan.documentqa.document;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentManagementService documentManagementService;

    public DocumentController(
            DocumentManagementService documentManagementService
    ) {
        this.documentManagementService = documentManagementService;
    }

    @GetMapping
    public List<DocumentResponse> listDocuments(
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {

        return documentManagementService
                .listDocuments(tenantId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @GetMapping("/{documentId}")
    public DocumentResponse getDocument(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable UUID documentId
    ) {

        return DocumentResponse.from(
                documentManagementService.getDocument(
                        tenantId,
                        documentId
                )
        );
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable UUID documentId
    ) {

        documentManagementService.deleteDocument(
                tenantId,
                documentId
        );

        return ResponseEntity.noContent().build();
    }
}