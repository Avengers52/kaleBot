# MVP

## Goal
Deliver a minimal self-hosted personal chatbot platform with a web UI, streaming chat, and basic ingestion stubs.

## Must-have
- Spring Boot backend with streaming chat endpoint (SSE)
- React frontend with message list + composer
- Postgres storage for ingested documents
- Docker Compose for local infra
- CI that builds backend and frontend

## Nice-to-have (next milestones)
- Add real URL ingestion + chunking
- Enable embeddings + pgvector search
- Add user authentication
- Add configurable prompt templates
- Add production-grade deployment scripts
