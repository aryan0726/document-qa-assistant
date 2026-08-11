-- =========================================================
-- Document Q&A Assistant
-- V1 - Initial Database Schema
-- =========================================================

-- ---------------------------------------------------------
-- Extensions
-- ---------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ---------------------------------------------------------
-- Documents
-- ---------------------------------------------------------

CREATE TABLE documents (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           tenant_id VARCHAR(100) NOT NULL,

                           title VARCHAR(255) NOT NULL,
                           category VARCHAR(100),

                           filename VARCHAR(500) NOT NULL,

                           content_hash VARCHAR(64) NOT NULL,

                           size_bytes BIGINT NOT NULL,

                           status VARCHAR(30) NOT NULL,

                           error_message TEXT,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT uk_documents_tenant_content_hash
                               UNIQUE (tenant_id, content_hash),

                           CONSTRAINT chk_documents_status
                               CHECK (status IN ('PROCESSING', 'READY', 'FAILED')),

                           CONSTRAINT chk_documents_size
                               CHECK (size_bytes >= 0)
);


-- ---------------------------------------------------------
-- Document Chunks
-- ---------------------------------------------------------

CREATE TABLE document_chunks (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 document_id UUID NOT NULL,

                                 tenant_id VARCHAR(100) NOT NULL,

                                 chunk_index INTEGER NOT NULL,

                                 content TEXT NOT NULL,

                                 page_number INTEGER,

                                 token_count INTEGER,

                                 embedding VECTOR(768),

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_document_chunks_document
                                     FOREIGN KEY (document_id)
                                         REFERENCES documents(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT uk_document_chunks_document_index
                                     UNIQUE (document_id, chunk_index),

                                 CONSTRAINT chk_document_chunks_index
                                     CHECK (chunk_index >= 0),

                                 CONSTRAINT chk_document_chunks_page
                                     CHECK (page_number IS NULL OR page_number > 0),

                                 CONSTRAINT chk_document_chunks_token_count
                                     CHECK (token_count IS NULL OR token_count >= 0)
);


-- ---------------------------------------------------------
-- Conversations
-- ---------------------------------------------------------

CREATE TABLE conversations (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               tenant_id VARCHAR(100) NOT NULL,

                               title VARCHAR(255),

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               last_message_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------
-- Messages
-- ---------------------------------------------------------

CREATE TABLE messages (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          conversation_id UUID NOT NULL,

                          role VARCHAR(30) NOT NULL,

                          content TEXT NOT NULL,

                          token_count INTEGER,

                          model VARCHAR(150),

                          latency_ms BIGINT,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_messages_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_messages_role
                              CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM')),

                          CONSTRAINT chk_messages_token_count
                              CHECK (token_count IS NULL OR token_count >= 0),

                          CONSTRAINT chk_messages_latency
                              CHECK (latency_ms IS NULL OR latency_ms >= 0)
);


-- ---------------------------------------------------------
-- Message Sources
-- ---------------------------------------------------------

CREATE TABLE message_sources (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 message_id UUID NOT NULL,

                                 chunk_id UUID NOT NULL,

                                 similarity_score DOUBLE PRECISION NOT NULL,

                                 CONSTRAINT fk_message_sources_message
                                     FOREIGN KEY (message_id)
                                         REFERENCES messages(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_message_sources_chunk
                                     FOREIGN KEY (chunk_id)
                                         REFERENCES document_chunks(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT chk_message_sources_similarity
                                     CHECK (
                                         similarity_score >= 0
                                             AND similarity_score <= 1
                                         )
);


-- =========================================================
-- Indexes
-- =========================================================

-- Tenant-scoped document listing
CREATE INDEX idx_documents_tenant_created_at
    ON documents (tenant_id, created_at DESC);

-- Tenant/category filtering
CREATE INDEX idx_documents_tenant_category
    ON documents (tenant_id, category);

-- Document status queries
CREATE INDEX idx_documents_status
    ON documents (status);

-- Tenant-scoped chunk retrieval
CREATE INDEX idx_document_chunks_tenant_document
    ON document_chunks (tenant_id, document_id);

-- Page-level ordering
CREATE INDEX idx_document_chunks_document_page
    ON document_chunks (document_id, page_number, chunk_index);

-- Conversation lookup
CREATE INDEX idx_conversations_tenant_last_message
    ON conversations (tenant_id, last_message_at DESC);

-- Message history
CREATE INDEX idx_messages_conversation_created
    ON messages (conversation_id, created_at);

-- Source lookup
CREATE INDEX idx_message_sources_message
    ON message_sources (message_id);

-- Chunk source lookup
CREATE INDEX idx_message_sources_chunk
    ON message_sources (chunk_id);


-- =========================================================
-- Vector Index
-- =========================================================

CREATE INDEX idx_document_chunks_embedding_hnsw
    ON document_chunks
    USING hnsw (embedding vector_cosine_ops);