# Architecture

```
+------------------+        SSE/HTTP        +---------------------+
|   React (Vite)   | <--------------------> |  Spring Boot API     |
|  Chat UI + RAG   |                        |  Chat + Ingestion    |
+------------------+                        +----------+----------+
                                                       |
                                                       | JDBC
                                                       v
                                              +-------------------+
                                              |  Postgres +       |
                                              |  pgvector         |
                                              +-------------------+

Optional:
+------------------+
|  Ollama Runtime  |
+------------------+
```

## Component responsibilities

### Frontend
- Renders message history, composer, and sources placeholder
- Streams chat responses over SSE

### Backend
- Routes HTTP requests
- Orchestrates chat flow (RAG + model streaming)
- Accepts ingestion requests and stores raw documents

### Postgres + pgvector
- Stores ingested documents and future embeddings

### Ollama (optional)
- Streams model responses when `MODEL_PROVIDER=ollama`
