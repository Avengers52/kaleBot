package com.kalebot.service;

import com.kalebot.core.IngestionService;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleIngestionService implements IngestionService {
  private static final Logger logger = LoggerFactory.getLogger(SimpleIngestionService.class);
  private final JdbcTemplate jdbcTemplate;

  public SimpleIngestionService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void ingestUrl(String url) {
    logger.info("Received URL ingestion request: {}", url);
    // TODO: Fetch the URL content and store chunks.
  }

  @Override
  public void ingestText(String sourceId, String text) {
    if (sourceId == null || sourceId.isBlank()) {
      throw new IllegalArgumentException("sourceId is required");
    }
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("text is required");
    }
    jdbcTemplate.update(
        "INSERT INTO ingested_docs (source_id, content, created_at) VALUES (?, ?, ?)",
        sourceId,
        text,
        OffsetDateTime.now()
    );
    logger.info("Stored ingested text for sourceId={}", sourceId);
  }
}
