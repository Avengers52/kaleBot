package com.kalebot.service;

import com.kalebot.core.IngestionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "vectorstore.enabled", havingValue = "false", matchIfMissing = true)
public class NoopIngestionService implements IngestionService {
  @Override
  public void ingestUrl(String url) {
    throw new IllegalStateException(
        "Ingestion is disabled. Set VECTORSTORE_ENABLED=true and configure Postgres to enable it."
    );
  }

  @Override
  public void ingestText(String sourceId, String text) {
    throw new IllegalStateException(
        "Ingestion is disabled. Set VECTORSTORE_ENABLED=true and configure Postgres to enable it."
    );
  }
}
