package com.aryan.documentqa.document;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentUploadService documentUploadService;

    public DocumentController(
            DocumentUploadService documentUploadService
    ) {
        this.documentUploadService = documentUploadService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam("title") String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        DocumentUploadResponse response =
                documentUploadService.upload(
                        tenantId,
                        title,
                        category,
                        file
                );

        return ResponseEntity.accepted().body(response);
    }
}