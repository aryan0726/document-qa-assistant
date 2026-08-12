package com.aryan.documentqa.retrieval;

import com.aryan.documentqa.TestcontainersConfiguration;
import com.aryan.documentqa.document.Document;
import com.aryan.documentqa.document.DocumentChunk;
import com.aryan.documentqa.document.DocumentChunkRepository;
import com.aryan.documentqa.document.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DocumentCategoryFilteringIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @BeforeEach
    void setUp() {

        documentChunkRepository.deleteAllInBatch();
        documentRepository.deleteAllInBatch();

        Document policyDocument =
                new Document(
                        "school-006",
                        "School Policy",
                        "policy",
                        "policy.txt",
                        "hash-policy",
                        100L
                );

        policyDocument.markReady();

        Document handbookDocument =
                new Document(
                        "school-006",
                        "Student Handbook",
                        "handbook",
                        "handbook.txt",
                        "hash-handbook",
                        100L
                );

        handbookDocument.markReady();

        Document savedPolicy =
                documentRepository.save(policyDocument);

        Document savedHandbook =
                documentRepository.save(handbookDocument);

        DocumentChunk policyChunk =
                new DocumentChunk(
                        savedPolicy.getId(),
                        "school-006",
                        0,
                        "School policy information",
                        1,
                        10
                );

        policyChunk.setEmbedding(
                createEmbedding(1.0f)
        );

        DocumentChunk handbookChunk =
                new DocumentChunk(
                        savedHandbook.getId(),
                        "school-006",
                        0,
                        "Student handbook information",
                        1,
                        10
                );

        handbookChunk.setEmbedding(
                createEmbedding(1.0f)
        );

        documentChunkRepository.saveAll(
                List.of(
                        policyChunk,
                        handbookChunk
                )
        );
    }

    @Test
    void shouldReturnOnlyChunksMatchingRequestedCategory() {

        String vector =
                toPgVector(createEmbedding(1.0f));

        List<Object[]> results =
                documentChunkRepository.findSimilarChunks(
                        "school-006",
                        vector,
                        "policy",
                        0.70,
                        5
                );

        assertEquals(1, results.size());

        Object[] result = results.get(0);

        assertEquals(
                "school-006",
                result[2]
        );

        assertEquals(
                "School policy information",
                result[4]
        );

        assertTrue(
                results.stream()
                        .allMatch(row ->
                                "School policy information"
                                        .equals(row[4])
                        )
        );
    }

    @Test
    void shouldReturnAllCategoriesWhenCategoryIsNull() {

        String vector =
                toPgVector(createEmbedding(1.0f));

        List<Object[]> results =
                documentChunkRepository.findSimilarChunks(
                        "school-006",
                        vector,
                        null,
                        0.70,
                        5
                );

        assertEquals(2, results.size());
    }

    private float[] createEmbedding(float value) {

        float[] embedding =
                new float[768];

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = value;
        }

        return embedding;
    }

    private String toPgVector(float[] embedding) {

        StringBuilder vector =
                new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                vector.append(",");
            }

            vector.append(embedding[i]);
        }

        vector.append("]");

        return vector.toString();
    }
}