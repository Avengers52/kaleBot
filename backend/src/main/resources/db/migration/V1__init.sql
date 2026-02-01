CREATE TABLE IF NOT EXISTS ingested_docs (
  id SERIAL PRIMARY KEY,
  source_id VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vector_chunks (
  id SERIAL PRIMARY KEY,
  doc_id INTEGER NOT NULL REFERENCES ingested_docs(id),
  chunk_text TEXT NOT NULL,
  embedding VECTOR(1536),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
