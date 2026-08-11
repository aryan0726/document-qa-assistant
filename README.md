# Document Q&A Assistant

A multi-tenant backend service that ingests school documents and answers natural-language questions using Retrieval-Augmented Generation (RAG), with citations back to the source document and page.

> **Project status:** Foundation phase — Spring Boot, Spring AI, Gemini, PostgreSQL/pgvector dependencies, Flyway, Testcontainers, environment configuration, and Git/GitHub setup are complete. Core document ingestion and retrieval features are under development.

---

## 1. Overview

The Document Q&A Assistant is a backend service designed for organizations that need to answer questions from a collection of documents such as:

- Fee policies
- Transport rules
- Exam circulars
- HR policies
- Admission procedures

The system will ingest documents, extract and chunk their content, generate embeddings, store those embeddings in PostgreSQL with pgvector, and retrieve relevant chunks when a user asks a question.

The final answer will be grounded in retrieved document content and will include citations to the source document and page.

---

## 2. Assignment Goals

The system is being built to satisfy the following core requirements:

- Document upload for PDF, DOCX, TXT, and Markdown files
- Asynchronous document ingestion
- Text extraction with page/section information preserved
- Configurable chunking with overlap
- Batched embedding generation
- PostgreSQL + pgvector vector storage
- Database-level metadata filtering
- Similarity threshold and top-K retrieval
- Grounded answers with source citations
- Refusal when no retrieved chunk meets the configured threshold
- Conversation memory
- Multi-tenant data isolation
- Document deletion
- Streaming responses using Server-Sent Events
- Testcontainers integration tests
- Observability and health checks

---

## 3. Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| AI Framework | Spring AI 2.0.0 |
| LLM Provider | Google Gemini |
| Embeddings | Google Gemini |
| Database | PostgreSQL |
| Vector Store | pgvector |
| Database Migrations | Flyway |
| Testing | JUnit + Testcontainers |
| Build Tool | Maven Wrapper |
| Containerization | Docker / Docker Compose |
| API | REST + Server-Sent Events |
| Version Control | Git + GitHub |

The assignment requires Java 21/25 LTS, Spring Boot 4.x, Spring AI 2.x, PostgreSQL + pgvector, migrations, and environment-based API credentials. The selected stack follows those constraints.

---

## 4. Architecture

### High-Level Architecture

```text
                         ┌─────────────────────┐
                         │      Client         │
                         │  Swagger / REST UI  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Spring Boot      │
                         │      REST API       │
                         └──────────┬──────────┘
                                    │
                ┌───────────────────┴───────────────────┐
                │                                       │
                ▼                                       ▼
        Document Upload                           Chat Request
                │                                       │
                ▼                                       ▼
       Async Ingestion                           Query Embedding
                │                                       │
                ▼                                       ▼
        Text Extraction                         pgvector Search
                │                                       │
                ▼                                       │
            Chunking                                   │
                │                                       │
                ▼                                       │
        Gemini Embeddings                               │
                │                                       │
                └──────────────► PostgreSQL ◄────────────┘
                                      │
                                      ▼
                              Retrieved Context
                                      │
                                      ▼
                                  Gemini
                                      │
                                      ▼
                              Grounded Answer
                                      │
                                      ▼
                                  Sources

## 5. Document Ingestion Flow

The planned ingestion pipeline is:
Upload Document
      │
      ▼
Validate File
      │
      ▼
Calculate SHA-256
      │
      ▼
Create Document Record
(status = PROCESSING)
      │
      ▼
Asynchronous Processing
      │
      ▼
Extract Text + Page Information
      │
      ▼
Chunk Text
      │
      ▼
Generate Embeddings in Batches
      │
      ▼
Persist Chunks + Embeddings
      │
      ▼
Update Document Status
      │
      ├── READY
      │
      └── FAILED

The ingestion process will be asynchronous so that processing a large document does not block an HTTP request thread.

## 6. Question / Retrieval Flow

The planned query pipeline is:
User Question
      │
      ▼
Validate Tenant
      │
      ▼
Create Query Embedding
      │
      ▼
Vector Search in PostgreSQL
      │
      ├── Tenant filter
      ├── Optional category filter
      ├── Top-K
      └── Similarity threshold
      │
      ▼
Relevant Chunks
      │
      ├── No qualifying chunks
      │          │
      │          ▼
      │      Refusal Response
      │
      ▼
Build Grounded Prompt
      │
      ▼
Gemini
      │
      ▼
Answer + Sources

Retrieval filtering will be performed at the database/vector-search level rather than retrieving unrelated chunks and filtering them afterward in Java.


## 7. Multi-Tenancy

Every request will carry a tenant identifier.

The initial API design will use:
X-Tenant-Id

Documents, document chunks, and conversations will be scoped by tenant.

The retrieval layer must ensure that a tenant can never retrieve chunks belonging to another tenant.

Tenant isolation is a core correctness requirement of the assignment and will be covered by integration tests.

## 8. Database

The planned relational model contains:

documents
document_chunks
conversations
messages
message_sources

Conceptually:
documents
    │
    └── document_chunks
              │
              └── vector embeddings

conversations
    │
    └── messages
              │
              └── message_sources
                         │
                         └── document_chunks

Database schema changes will be managed using Flyway migrations.

Hibernate schema auto-update will not be used.

## 9. Embeddings

The current implementation uses Google Gemini embeddings.

The embedding model, vector dimensions, batching strategy, and estimated cost will be documented here after the ingestion implementation and evaluation are finalized.

Current Configuration
Provider: Google Gemini
Vector Store: PostgreSQL + pgvector
Index: HNSW
Distance: Cosine
The exact embedding dimensions will remain aligned with the selected Gemini embedding model and the final database migration.

## 10. Chunking Strategy

The assignment requires the chunking strategy, chunk size, overlap, and reasoning to be documented.

The final values will be recorded here after implementation and evaluation.

The decision will consider:

Semantic coherence
Retrieval accuracy
Context size
Page preservation
Embedding cost
Retrieval latency

Example evaluation questions and observed retrieval behavior will be documented when the retrieval pipeline is implemented.

## 11. Similarity Threshold

A similarity threshold will be used to determine whether retrieved chunks provide sufficient evidence to answer a question.

The final threshold will not be selected arbitrarily.

It will be evaluated against:

In-scope factual questions
Multi-hop questions
Follow-up questions
Category-filtered questions
Near-miss questions
Out-of-scope questions

The final README will document the chosen threshold and the evidence used to select it.

## 12. Grounding and Refusal

The system will instruct the LLM to answer only from the retrieved context.

If no retrieved chunk satisfies the configured similarity threshold, the application will return a fixed refusal response instead of calling the LLM.

This prevents unsupported answers when the document corpus does not contain sufficient information.

Every factual claim in a successful response should be traceable to one of the returned source chunks.

## 13. Conversation Memory

Conversation turns will be stored in PostgreSQL.

The planned chat flow supports follow-up questions such as:

User:
What is the late fee for term 2?

Assistant:
...

User:
What about for class 9?

The system will include relevant recent conversation history while respecting a configurable token budget.

## 14. Streaming

The application will provide a streaming chat endpoint using Server-Sent Events.

Planned behavior:

POST /api/v1/chat/stream

        │
        ▼
   Retrieve Context
        │
        ▼
   Call Gemini
        │
        ▼
 Stream Tokens
        │
        ▼
 Sources Event

A disconnected client should cancel the upstream model request rather than leaving an orphaned request running.

## 15. API Endpoints

The planned API includes:

Documents
POST   /api/v1/documents
GET    /api/v1/documents
GET    /api/v1/documents/{id}
DELETE /api/v1/documents/{id}
Chat
POST /api/v1/chat
POST /api/v1/chat/stream
Conversations
GET /api/v1/conversations/{id}
Health
GET /actuator/health

The exact request and response schemas will be documented as the controllers are implemented.

## 16. Environment Variables

Create a local .env or configure equivalent environment variables.

Required variables:

GEMINI_API_KEY=

DB_URL=jdbc:postgresql://localhost:5432/document_qa
DB_USERNAME=postgres
DB_PASSWORD=postgres

SERVER_PORT=8080

See .env.example for the template.

Security

Never commit:

.env
API keys
passwords
credentials

The Gemini API key is read from an environment variable.

## 17. Local Development
Prerequisites

Install:

Java 21
Docker Desktop
Git

Maven does not need to be installed globally because the project includes the Maven Wrapper.

Verify:

java -version
docker --version
docker compose version

Run Maven commands using:

.\mvnw.cmd
## 18. Running Tests

Run the test suite with:

.\mvnw.cmd clean test "-Duser.timezone=Asia/Kolkata"

Integration tests use Testcontainers and a real PostgreSQL environment rather than H2.

Tests must also be designed so that the suite can run without requiring a real Gemini API key.

## 19. Project Structure
document-qa-assistant/
│
├── .env.example
├── .gitignore
├── .gitattributes
├── pom.xml
├── mvnw
├── mvnw.cmd
│
├── .mvn/
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/aryan/documentqa/
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        └── java/
            └── com/aryan/documentqa/

The package structure will evolve as the domain, controller, service, repository, ingestion, retrieval, and infrastructure layers are implemented.

## 20. Testing Strategy

The project will include:

Unit Tests

Tests for:

Chunking boundaries
Empty files
Single-word documents
Documents larger than one chunk
Validation logic
Refusal logic
Integration Tests

Using Testcontainers with real PostgreSQL + pgvector.

Important scenarios include:

Document persistence
Vector retrieval
Tenant isolation
Category filtering
Document deletion
Refusal when no chunk meets the threshold

The model provider will be mocked or stubbed in tests so the test suite does not require an API key.

## 21. Observability

The application will provide:

Correlation/request IDs
Retrieval latency metrics
Model latency metrics
Token counts
Estimated model cost
Database health
Model-provider health

Sensitive information, PII, API keys, and full document contents must not appear in logs.

## 22. Performance Targets

The implementation will target the assignment's stated requirements:

Retrieval under 500 ms for a corpus of approximately 200 documents
First streamed token under 3 seconds
50-page document ingestion without blocking an HTTP request thread

Actual measurements will be documented after implementation and testing.

## 23. Known Limitations

This section will be updated honestly as implementation progresses.

Current limitations:

Core document ingestion pipeline is not implemented yet.
Retrieval and chat functionality are not implemented yet.
Streaming is not implemented yet.
Production-grade resilience is not implemented yet.
Threshold evaluation has not yet been performed.
Final embedding cost analysis has not yet been performed.

## 24. Development Roadmap
Phase 1 — Foundation
 Spring Boot project
 Java 21
 Spring AI
 Gemini configuration
 PostgreSQL dependencies
 pgvector dependency
 Flyway
 Testcontainers
 Environment-based secrets
 Git repository
 GitHub repository
Phase 2 — Database
 Docker Compose with PostgreSQL + pgvector
 Flyway migrations
 Document entity
 Document chunk entity
 Conversation entity
 Message entity
 Message source entity
 Vector index
Phase 3 — Document Ingestion
 Multipart upload
 File validation
 SHA-256 idempotency
 PDF extraction
 DOCX extraction
 TXT/Markdown extraction
 Chunking
 Batched embeddings
 Async processing
 Status transitions
Phase 4 — Retrieval & Chat
 Query embeddings
 Tenant filtering
 Category filtering
 Top-K retrieval
 Similarity threshold
 Grounded prompt
 Refusal path
 Source citations
Phase 5 — Conversation & Streaming
 Conversation persistence
 Token-budgeted history
 SSE streaming
 Client disconnect cancellation
Phase 6 — Reliability & Observability
 Correlation IDs
 Metrics
 Retry
 Timeout
 Circuit breaker
 Health checks
 Clean error responses
Phase 7 — Testing & Evaluation
 Unit tests
 Integration tests
 Tenant isolation tests
 Retrieval evaluation
 Refusal evaluation
 Coverage target
 Performance measurements
Phase 8 — Submission
 Final README
 Clean-clone verification
 5-minute demo
 Git history review
 Security review
 Final cleanup
## 25. Design Decisions

Important implementation decisions will be recorded here as the project evolves.

Each decision should include:

What was chosen
Why it was chosen
Alternatives considered
Evidence or measurements when applicable

This is especially important for:

Chunk size and overlap
Embedding model
Similarity threshold
Retrieval K
Conversation history limits
Async ingestion strategy
Database indexing
## 26. Assignment Reference

This project is implemented according to the provided Document Q&A Assistant (RAG) Engineering Assignment.

The priority is correctness of:

Grounded answers
Refusal behavior
Tenant isolation
Database-level retrieval filtering
Real-world reliability

Stretch goals will only be implemented after the core requirements are stable and tested.

## 27. License

This project was created as an engineering assignment.