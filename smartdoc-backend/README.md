# Step 07 — Spring Boot REST API

Transform the plain-Java LangChain4j application (Step 06) into a produciton-ready Spring Boot multi-module web service with REST and SSE endpoints.

---

## Learning Objectives

- Understand the **Spring Boot multi-module** architecture and its benefits
- Learn how `@Configuration` + `@Bean` replaces `main()` manual assembly
- Implement **SSE (Server-Sent Events)** streaming with `SseEmitter`
- Use `@ConfigurationProperties` for externalized configuration
- Decouple AI components into separate Maven modules
- Expose REST endpoints for chat, document upload, and session management
- See how Step 06's plain Java code maps 1:1 to Spring Boot constructs

---

## Module Overview

```
smartdoc-agent (parent POM)
├── smartdoc-llm/       — LLM provider config (ChatModel, StreamingChatModel, EmbeddingModel)
├── smartdoc-rag/       — RAG pipeline (document ingestion, vector store, content retrieval)
├── smartdoc-tools/     — Agent tools (KnowledgeSearchTool, TaskStatusTool)
├── smartdoc-chat/      — AiService assembly (ChatConfig, DocAssistant, ChatSessionManager)
└── smartdoc-api/       — REST controllers (ChatController, DocumentController)
```

Each module corresponds to a section of Step 06's `main()` method:

| Module | Step 06 Equivalent | Responsibility |
|--------|-------------------|----------------|
| `smartdoc-llm` | `createChatModel()`, `createStreamingChatModel()` | LLM client beans |
| `smartdoc-rag` | `RagConfig.createRetriever()`, document ingestion | Embedding + retrieval |
| `smartdoc-tools` | `KnowledgeSearchTool`, `TaskStatusTool` | `@Tool` agent methods |
| `smartdoc-chat` | `AiServices.builder()...build()` | Assembly + session mgmt |
| `smartdoc-api` | `main()` loop → HTTP | REST/SSE endpoints |

---

## Quick Start

```bash
# 1. Set your API Key
set LLM_PROVIDER=deepseek
set DEEPSEEK_API_KEY=your-key

# 2. Build the parent project
cd smartdoc-backend
mvn clean install -DskipTests

# 3. Start the server (from smartdoc-api)
cd smartdoc-api
mvn spring-boot:run

# 4. Test the chat endpoint
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello", "sessionId": "test-001"}'
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | SSE streaming chat — sends tokens as Server-Sent Events |
| `GET` | `/api/chat/history/{sessionId}` | Returns conversation history as `[{role, content}]` |
| `DELETE` | `/api/chat/session/{sessionId}` | Clears session memory on the server |
| `POST` | `/api/documents/upload` | Upload a document (PDF/DOCX/TXT) for RAG ingestion |
| `GET` | `/api/documents` | List uploaded document filenames |

### SSE Chat Response Format

```
data:Hello
data: world
data:!
data:[DONE]
```

Each `data:` line is one token. `[DONE]` marks the end of the stream.

---

## Step 06 to Step 07 Mapping

This is the core learning objective — every piece of Step 06's plain Java code has a direct Spring Boot equivalent:

| Step 06 (Plain Java) | Step 07 (Spring Boot) |
|---|---|
| `new ZhipuAiChatModel(...)` | `@Bean ChatModel` in `LlmConfig` |
| `new InMemoryEmbeddingStore<>()` | `@Bean EmbeddingStore` in `EmbeddingConfig` |
| `EmbeddingStoreIngestor.builder(...)` | `@Service DocumentIngestionService` |
| `EmbeddingStoreContentRetriever.builder(...)` | `@Service RetrievalService` |
| `ConcurrentHashMap<String, ChatMemory>` | `@Component ChatSessionManager` |
| `AiServices.builder()...build()` | `@Configuration ChatConfig.docAssistant()` |
| `main()` → `Scanner.nextLine()` | `@RestController` → HTTP endpoints |
| Hardcoded config values | `application.yml` + `LlmProperties` |

---

## Configuration

### `application.yml`

```yaml
llm:
  provider: ${LLM_PROVIDER:zhipu}   # Switch: zhipu / deepseek / openai
  zhipu:
    base-url: https://open.bigmodel.cn/
    api-key: ${ZHIPU_API_KEY:}
    model-name: glm-4-flash
  deepseek:
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:}
    model-name: deepseek-chat

rag:
  chunk-size: 500
  chunk-overlap: 100
  max-results: 5

chat:
  max-memory-messages: 20
```

All sensitive values use environment variable placeholders (`${VAR:default}`).

---

## Key Architecture Decisions

### Why Multi-Module?

- **Separation of concerns**: Each module has a single responsibility
- **Independent versioning**: RAG or Tools can evolve without affecting the API
- **Testability**: Modules can be tested in isolation
- **Production readiness**: Mirrors real enterprise project structure

### Why SSE Instead of WebSocket?

SSE (Server-Sent Events) is simpler for one-directional streaming (server → client):
- Works over standard HTTP — no protocol upgrade
- Built-in support in Spring MVC (`SseEmitter`)
- Easy to consume in the browser (`fetch()` + `ReadableStream`)
- Automatic reconnection in native `EventSource` API

For Step 08 (Vue frontend), we use `fetch()` + `ReadableStream` instead of `EventSource` because we need `POST` to send the chat message.

### Provider Switching

The `LlmConfig` reads the active provider from configuration and creates the appropriate `ChatModel` / `StreamingChatModel` implementation:
- **Zhipu**: Uses `langchain4j-community-zhipu-ai` with native SDK
- **DeepSeek / OpenAI**: Uses `langchain4j-open-ai` (OpenAI-compatible API)

---

## Technologies

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5.0 + Java 17 |
| AI | LangChain4j 1.13.0 |
| LLM Clients | Zhipu AI, OpenAI (also covers DeepSeek) |
| Embedding | All-MiniLM-L6-v2 (local ONNX) |
| Document Parsing | Apache PDFBox + Apache POI |
| Build | Maven multi-module |

---

## Difference from Step 06

- Step 06: Single Java file, console I/O, hardcoded config
- **Step 07**: Multi-module Maven, REST/SSE API, externalized config, DI

---

## Next Step

[Step 08: Vue 3 Frontend](../smartdoc-frontend/) — Build a browser-based chat UI that consumes this API
