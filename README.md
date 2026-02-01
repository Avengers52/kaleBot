# kaleBot

Minimal skeleton for a self-hosted personal chatbot platform. This repository is intentionally lightweight with stubbed services and TODOs so you can iterate quickly.

## Prerequisites
- Java 17
- Node.js 18+
- Docker + Docker Compose
- PostgreSQL (if running without Docker)

> Note: `backend/mvnw` is a thin wrapper that invokes your local `mvn` binary.

## Repository layout
```
/
  backend/      # Spring Boot API
  frontend/     # React + Vite UI
  docker/       # Compose + Postgres init
  docs/         # MVP, architecture, API docs
```

## Local development

### Backend
```
cd backend
cp .env.example .env
./mvnw spring-boot:run
```

### Frontend
```
cd frontend
cp .env.example .env
npm install
npm run dev
```

## Docker Compose
```
docker compose -f docker/compose.yml up --build
```

Enable Ollama with:
```
MODEL_PROVIDER=ollama docker compose -f docker/compose.yml --profile ollama up --build
```

Optionally run the frontend container:
```
docker compose -f docker/compose.yml --profile frontend up --build
```

## Configuration
Environment variables (see `.env.example` files):
- `DB_URL`, `DB_USER`, `DB_PASS`
- `MODEL_PROVIDER=mock|ollama`
- `OLLAMA_BASE_URL`
- `OLLAMA_MODEL`
- `OLLAMA_EMBED_MODEL` (optional)
- `OSV_BASE_URL`
- `NVD_BASE_URL`
- `NVD_API_KEY` (optional)
- `FRONTEND_ORIGIN`

## Endpoints
- `GET /api/health`
- `POST /api/chat/stream` (SSE)
- `POST /api/ingest/url`
- `POST /api/ingest/text`
- `POST /api/vuln/scan`

See [docs/API.md](docs/API.md) for request/response examples.

### Vulnerability scan example
```
curl -X POST http://localhost:8080/api/vuln/scan \\
  -H 'Content-Type: application/json' \\
  -d '{
    "inputType": "maven_pom",
    "content": "<project>...</project>",
    "includeTransitives": false
  }'
```

### Chat stream example (SSE)
```
curl -N -X POST http://localhost:8080/api/chat/stream \\
  -H 'Content-Type: application/json' \\
  -d '{
    "message": "<project>...</project>"
  }'
```

## Where to add real logic
- Replace mock model streaming in `MockModelClient` with real providers or extend `OllamaModelClient`.
- Fill in `SimpleRagService`, `SimpleEmbeddingService`, and `SimpleVectorStoreService` for RAG.
- Expand ingestion logic in `SimpleIngestionService` to fetch URLs, chunk text, and store embeddings.

## CI
GitHub Actions runs backend tests, frontend build, and Docker image builds on every push/PR.
