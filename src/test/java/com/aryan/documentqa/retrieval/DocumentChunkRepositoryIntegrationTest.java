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
class DocumentChunkRepositoryIntegrationTest {

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {

        documentChunkRepository.deleteAllInBatch();
        documentRepository.deleteAllInBatch();

        Document school006Document =
                new Document(
                        "school-006",
                        "School 006 Document",
                        "policy",
                        "school006.txt",
                        "hash-school-006",
                        100L
                );

        school006Document.markReady();

        Document school007Document =
                new Document(
                        "school-007",
                        "School 007 Document",
                        "policy",
                        "school007.txt",
                        "hash-school-007",
                        100L
                );

        school007Document.markReady();

        Document savedSchool006 =
                documentRepository.save(school006Document);

        Document savedSchool007 =
                documentRepository.save(school007Document);

        DocumentChunk school006Chunk =
                new DocumentChunk(
                        savedSchool006.getId(),
                        "school-006",
                        0,
                        "School 006 confidential document content",
                        1,
                        10
                );

        school006Chunk.setEmbedding(
                createEmbedding(1.0f)
        );

        DocumentChunk school007Chunk =
                new DocumentChunk(
                        savedSchool007.getId(),
                        "school-007",
                        0,
                        "School 007 confidential document content",
                        1,
                        10
                );

        school007Chunk.setEmbedding(
                createEmbedding(1.0f)
        );

        documentChunkRepository.saveAll(
                List.of(
                        school006Chunk,
                        school007Chunk
                )
        );
    }

    @Test
    void shouldNeverReturnChunksFromAnotherTenant() {

        float[] queryEmbedding =
                createEmbedding(1.0f);

        String vector =
                toPgVector(queryEmbedding);

        List<Object[]> results =
                documentChunkRepository.findSimilarChunks(
                        "school-007",
                        vector,
                        null,
                        0.70,
                        5
                );

        assertEquals(1, results.size());

        Object[] result = results.get(0);

        assertEquals(
                "school-007",
                result[2]
        );

        assertTrue(
                results.stream()
                        .allMatch(row ->
                                "school-007".equals(row[2])
                        )
        );
    }

    private float[] createEmbedding(float value) {

        float[] embedding = new float[768];

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