package com.kalebot.service;

import com.kalebot.core.VectorStoreService;
import com.kalebot.model.EmbeddedChunk;
import com.kalebot.model.SourceChunk;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleVectorStoreService implements VectorStoreService {
  private final JdbcTemplate jdbcTemplate;

  public SimpleVectorStoreService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void upsert(long docId, List<EmbeddedChunk> chunks) {
    String sql = """
        INSERT INTO vector_chunks (doc_id, chunk_text, embedding, created_at)
        VALUES (?, ?, ?::vector, ?)
        """;
    OffsetDateTime now = OffsetDateTime.now();
    for (EmbeddedChunk chunk : chunks) {
      jdbcTemplate.update(sql, docId, chunk.content(), toVectorLiteral(chunk.embedding()), now);
    }
  }

  @Override
  public List<SourceChunk> query(float[] queryVector, int topK) {
    String sql = """
        SELECT d.source_id, v.chunk_text
        FROM vector_chunks v
        JOIN ingested_docs d ON v.doc_id = d.id
        ORDER BY v.embedding <-> ?::vector
        LIMIT ?
        """;
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new SourceChunk(rs.getString("source_id"), rs.getString("chunk_text")),
        toVectorLiteral(queryVector),
        topK
    );
  }

  private String toVectorLiteral(float[] vector) {
    return "[" + java.util.Arrays.stream(vector)
        .mapToObj(Float::toString)
        .collect(Collectors.joining(",")) + "]";
  }
}
