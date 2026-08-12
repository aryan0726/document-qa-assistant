package com.aryan.documentqa.ingestion;

import com.aryan.documentqa.document.DocumentChunk;
import com.aryan.documentqa.document.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentEmbeddingService(
            EmbeddingModel embeddingModel,
            DocumentChunkRepository documentChunkRepository
    ) {
        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
    }

    public void embedChunks(List<DocumentChunk> chunks) {

        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        List<String> texts = chunks.stream()
                .map(DocumentChunk::getContent)
                .toList();

        List<float[]> embeddings = embeddingModel.embed(texts);

        if (embeddings.size() != chunks.size()) {
            throw new IllegalStateException(
                    "Embedding count does not match chunk count"
            );
        }

        for (int i = 0; i < chunks.size(); i++) {

            float[] embedding = embeddings.get(i);

            if (embedding == null) {
                throw new IllegalStateException(
                        "Embedding generation returned null for chunk " + i
                );
            }

            if (embedding.length != 768) {
                throw new IllegalStateException(
                        "Expected 768-dimensional embedding but received "
                                + embedding.length
                );
            }

            chunks.get(i).setEmbedding(embedding);
        }

        documentChunkRepository.saveAll(chunks);
    }
}