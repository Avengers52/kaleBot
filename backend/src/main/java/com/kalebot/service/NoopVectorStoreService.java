package com.kalebot.service;

import com.kalebot.core.VectorStoreService;
import com.kalebot.model.EmbeddedChunk;
import com.kalebot.model.SourceChunk;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "vectorstore.enabled", havingValue = "false", matchIfMissing = true)
public class NoopVectorStoreService implements VectorStoreService {
  private static final Logger logger = LoggerFactory.getLogger(NoopVectorStoreService.class);

  @Override
  public void upsert(long docId, List<EmbeddedChunk> chunks) {
    logger.warn("Vector store is disabled; skipping ingestion for docId={}", docId);
  }

  @Override
  public List<SourceChunk> query(float[] queryVector, int topK) {
    return List.of();
  }
}
