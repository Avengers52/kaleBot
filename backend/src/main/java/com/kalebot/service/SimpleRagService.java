package com.kalebot.service;

import com.kalebot.core.EmbeddingService;
import com.kalebot.core.RagService;
import com.kalebot.core.VectorStoreService;
import com.kalebot.model.SourceChunk;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimpleRagService implements RagService {
  private final EmbeddingService embeddingService;
  private final VectorStoreService vectorStoreService;
  private final int topK;

  public SimpleRagService(
      EmbeddingService embeddingService,
      VectorStoreService vectorStoreService,
      @Value("${rag.top-k:6}") int topK
  ) {
    this.embeddingService = embeddingService;
    this.vectorStoreService = vectorStoreService;
    this.topK = topK;
  }

  @Override
  public List<SourceChunk> retrieveContext(String query) {
    float[] embedding = embeddingService.embed(query);
    return vectorStoreService.query(embedding, topK);
  }
}
