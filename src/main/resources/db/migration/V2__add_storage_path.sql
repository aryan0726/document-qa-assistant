-- =========================================================
-- Document Q&A Assistant
-- V2 - Add document storage path
-- =========================================================

ALTER TABLE documents
    ADD COLUMN storage_path VARCHAR(1000);

CREATE INDEX idx_documents_tenant_storage_path
    ON documents (tenant_id, storage_path);