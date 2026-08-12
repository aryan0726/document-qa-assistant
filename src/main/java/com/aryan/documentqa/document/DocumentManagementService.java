package com.aryan.documentqa.document;

import com.aryan.documentqa.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentManagementService {

    private final DocumentRepository documentRepository;

    public DocumentManagementService(
            DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<Document> listDocuments(String tenantId) {

        validateTenantId(tenantId);

        return documentRepository
                .findAllByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public Document getDocument(
            String tenantId,
            UUID documentId
    ) {

        validateTenantId(tenantId);

        return documentRepository
                .findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found"
                        )
                );
    }

    @Transactional
    public void deleteDocument(
            String tenantId,
            UUID documentId
    ) {

        validateTenantId(tenantId);

        Document document = documentRepository
                .findByIdAndTenantId(documentId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found"
                        )
                );

        documentRepository.delete(document);
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
}