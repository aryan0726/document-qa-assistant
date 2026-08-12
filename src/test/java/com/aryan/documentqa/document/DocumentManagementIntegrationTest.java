package com.aryan.documentqa.document;

import com.aryan.documentqa.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DocumentManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    private UUID documentId;

    @BeforeEach
    void setUp() {

        documentRepository.deleteAll();

        Document document = new Document(
                "school-006",
                "Test Document",
                "policy",
                "test.txt",
                "test-content-hash",
                100L
        );

        document.markReady();

        Document saved = documentRepository.save(document);
        documentId = saved.getId();
    }

    @Test
    void shouldListDocumentsForTenant() throws Exception {

        mockMvc.perform(
                        get("/api/v1/documents")
                                .header("X-Tenant-Id", "school-006")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId")
                        .value("school-006"))
                .andExpect(jsonPath("$[0].title")
                        .value("Test Document"));
    }

    @Test
    void shouldNotAllowCrossTenantDocumentAccess() throws Exception {

        mockMvc.perform(
                        get("/api/v1/documents/" + documentId)
                                .header("X-Tenant-Id", "school-007")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetDocumentForCorrectTenant() throws Exception {

        mockMvc.perform(
                        get("/api/v1/documents/" + documentId)
                                .header("X-Tenant-Id", "school-006")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(documentId.toString()))
                .andExpect(jsonPath("$.tenantId")
                        .value("school-006"))
                .andExpect(jsonPath("$.title")
                        .value("Test Document"))
                .andExpect(jsonPath("$.status")
                        .value("READY"));
    }
}