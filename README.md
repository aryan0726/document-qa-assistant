# 📚 Document Q&A Assistant

A **multi-tenant Document Question & Answer Assistant** built with **Spring Boot, Spring AI, Google Gemini, PostgreSQL, pgvector, and Docker**.

The system allows users to upload documents, automatically extract and chunk their content, generate vector embeddings, perform semantic retrieval, and ask natural-language questions. Answers are generated using a Retrieval-Augmented Generation (RAG) pipeline with relevant document chunks as context.

---

## 🚀 Features

- 📄 Document upload and ingestion
- 🔤 Text extraction from documents
- ✂️ Intelligent document chunking
- 🧠 Gemini embedding generation
- 🔎 Semantic vector search using PostgreSQL + pgvector
- 🤖 Retrieval-Augmented Generation (RAG)
- 💬 Persistent conversations
- 📨 Persistent conversation messages
- 📂 Document management APIs
- 🔐 Multi-tenant data isolation
- ❌ Global exception handling
- 🗄️ Database migrations with Flyway
- 🐳 Dockerized application and PostgreSQL
- 🧪 Integration testing with Testcontainers
- ❤️ Health/monitoring endpoints through Spring Boot Actuator

---

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │      REST API       │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       Document APIs           Q&A APIs          Conversation APIs
              │                     │                     │
              ▼                     ▼                     ▼
       Document Service       Retrieval Service     Message Persistence
              │                     │
              ▼                     ▼
       Text Extraction          pgvector
              │                     │
              ▼                     ▼
          Chunking             Similarity Search
              │                     │
              ▼                     │
        Gemini Embeddings           │
              │                     │
              └──────────┬──────────┘
                         ▼
                  Relevant Context
                         │
                         ▼
                   Google Gemini
                         │
                         ▼
                      Answer

🔄 RAG Pipeline

The question-answering flow works as follows:
User Question
      │
      ▼
Generate Query Embedding
      │
      ▼
Semantic Search in pgvector
      │
      ▼
Retrieve Relevant Document Chunks
      │
      ▼
Build Context
      │
      ▼
Send Context + Question to Gemini
      │
      ▼
Generate Answer
      │
      ▼
Persist Conversation + Messages
      │
      ▼
Return Answer + Sources

🛠️ Tech Stack
Technology	Purpose
Java 21	Programming language
Spring Boot 4.1.0	Backend framework
Spring MVC	REST APIs
Spring Data JPA	Database persistence
Hibernate	ORM
Spring AI 2.0.0	AI integration
Google Gemini	LLM + embeddings
PostgreSQL	Relational database
pgvector	Vector similarity search
Flyway	Database migrations
Apache Tika	Document text extraction
Apache PDFBox	PDF processing
Docker	Containerization
Testcontainers	Integration testing
Maven	Build & dependency management
Spring Boot Actuator	Application monitoring


📦 Project Structure

src/
├── main/
│   ├── java/
│   │   └── com/aryan/documentqa/
│   │       │
│   │       ├── common/
│   │       │   ├── ApiErrorResponse.java
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   └── ResourceNotFoundException.java
│   │       │
│   │       ├── conversation/
│   │       │   ├── Conversation.java
│   │       │   ├── ConversationController.java
│   │       │   ├── ConversationRepository.java
│   │       │   ├── ConversationResponse.java
│   │       │   ├── Message.java
│   │       │   ├── MessageRepository.java
│   │       │   ├── MessageResponse.java
│   │       │   └── MessageRole.java
│   │       │
│   │       ├── document/
│   │       │   ├── Document.java
│   │       │   ├── DocumentChunk.java
│   │       │   ├── DocumentChunkRepository.java
│   │       │   ├── DocumentController.java
│   │       │   ├── DocumentExceptionHandler.java
│   │       │   ├── DocumentManagementService.java
│   │       │   ├── DocumentRepository.java
│   │       │   ├── DocumentResponse.java
│   │       │   ├── DocumentStatus.java
│   │       │   ├── DocumentUploadResponse.java
│   │       │   └── DocumentUploadService.java
│   │       │
│   │       ├── ingestion/
│   │       │   ├── ChunkedText.java
│   │       │   ├── DocumentChunker.java
│   │       │   ├── DocumentEmbeddingService.java
│   │       │   ├── DocumentProcessingService.java
│   │       │   ├── DocumentTextExtractor.java
│   │       │   ├── ExtractedDocument.java
│   │       │   └── ExtractedPage.java
│   │       │
│   │       ├── qa/
│   │       │   ├── AnswerGenerationResponse.java
│   │       │   ├── AnswerGenerationService.java
│   │       │   ├── QuestionAnswerController.java
│   │       │   └── QuestionRequest.java
│   │       │
│   │       ├── retrieval/
│   │       │   ├── DocumentRetrievalController.java
│   │       │   ├── DocumentRetrievalService.java
│   │       │   └── RetrievedChunk.java
│   │       │
│   │       └── storage/
│   │           └── FileStorageService.java
│   │
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── migration/
│               ├── V1__init_schema.sql
│               └── V2__add_storage_path.sql
│
└── test/
    └── java/
        └── com/aryan/documentqa/
            ├── DocumentQaAssistantApplicationTests.java
            ├── TestcontainersConfiguration.java
            ├── document/
            │   └── DocumentManagementIntegrationTest.java
            └── ingestion/
                └── DocumentChunkerTest.java


🔐 Multi-Tenant Architecture

Every document and conversation is associated with a tenant.

Requests use:
X-Tenant-Id: school-006

Tenant information is used when retrieving documents and conversations.

For example:school-006
   │
   └── Document A
       └── Chunks

school-007
   │
   └── Document B
       └── Chunks

A tenant cannot access another tenant's resources.

For example:
Document belongs to: school-006

Request:
X-Tenant-Id: school-007

Result:
404 Not Found
This prevents cross-tenant data leakage.

📡 API Endpoints
Documents
Upload document
POST /api/v1/documents

Headers:
X-Tenant-Id: school-006

Multipart fields:
title
category
file

Example:
curl.exe -X POST "http://localhost:8080/api/v1/documents" `
    -H "X-Tenant-Id: school-006" `
    -F "title=School Rules" `
    -F "category=policy" `
    -F "file=@sample.txt"

List documents
GET /api/v1/documents

Header:
X-Tenant-Id: school-006

Get document
GET /api/v1/documents/{documentId}

Header:
X-Tenant-Id: school-006

Delete document
DELETE /api/v1/documents/{documentId}

Header:
X-Tenant-Id: school-006

Semantic Retrieval
GET /api/v1/retrieval/search

Parameters:
query
limit

Example:
GET /api/v1/retrieval/search?query=What%20are%20the%20school%20library%20hours?&limit=5

Header:

X-Tenant-Id: school-006
Question Answering
POST /api/v1/qa/ask

Headers:

Content-Type: application/json
X-Tenant-Id: school-006

Request:

{
  "conversationId": null,
  "question": "What are the school library hours?"
}

Example response:

{
  "answer": "The school library is open from 8 AM to 5 PM.",
  "sources": [
    {
      "documentId": "70e0abbe-fd42-4c7e-ad6b-9c5ed870e4c3",
      "tenantId": "school-006",
      "chunkIndex": 0,
      "pageNumber": 1,
      "similarityScore": 0.77
    }
  ]
}
Conversations
GET /api/v1/conversations

Header:

X-Tenant-Id: school-006

Get messages:

GET /api/v1/conversations/{conversationId}/messages

Header:

X-Tenant-Id: school-006
🗄️ Database

The application uses PostgreSQL with pgvector.

Main tables:

documents
    │
    └── document_chunks

conversations
    │
    └── messages
            │
            └── message_sources

Document chunks store vector embeddings used for semantic retrieval.

Embeddings currently use:

768 dimensions

Database migrations are managed through Flyway.

V1__init_schema.sql
V2__add_storage_path.sql
🐳 Running with Docker

Build and start the application:

docker compose up -d --build

Check containers:

docker compose ps

Check application logs:

docker compose logs app --tail=50

The application runs on:

http://localhost:8080

PostgreSQL runs inside the Docker Compose network.

⚙️ Configuration

The application uses environment variables for sensitive configuration.

Important configuration includes:

GEMINI_API_KEY

Do not commit real API keys to Git.

Use environment-specific configuration for local development and deployment.

🧪 Testing

Run the complete Maven test suite:

.\mvnw.cmd clean test

The project contains:

Unit tests for document chunking
Spring Boot application tests
Document management integration tests
PostgreSQL Testcontainers integration

Current verified test result:

Tests run: 6
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
🔒 Security

The application implements tenant-scoped resource access.

Example:

school-006 → document belongs to school-006
school-007 → attempts to access document
                    ↓
                404 Not Found

Resource lookups use both:

documentId + tenantId

rather than relying only on the resource ID.

📊 Example RAG Result

For a document containing:

The school library is open from 8 AM to 5 PM.
Students must carry their identification cards.

Question:

What are the school library hours?

The system retrieves the relevant document chunk and generates:

The school library is open from 8 AM to 5 PM.

The response also contains the retrieved source chunk and similarity score.

🧰 Local Development
Requirements
Java 21
Maven Wrapper
Docker
Docker Compose
PostgreSQL with pgvector support
Google Gemini API key
Run tests
.\mvnw.cmd clean test
Start application
.\mvnw.cmd spring-boot:run

Or run everything with Docker:

docker compose up -d --build
📈 Future Improvements

Potential future improvements include:

Authentication with JWT/OAuth2
Role-based access control
Streaming LLM responses
Conversation title generation
Pagination for document and conversation APIs
Advanced document filtering
Hybrid keyword + vector retrieval
Reranking retrieved chunks
Redis caching
Rate limiting
Observability with metrics and tracing
Cloud deployment
Frontend application

👨‍💻 Author

Aryan Raj

B.Tech Computer Science & Engineering