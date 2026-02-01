package com.kalebot.service;

import com.kalebot.core.VectorStoreService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SimpleVectorStoreService implements VectorStoreService {
  @Override
  public void upsert(List<String> chunks) {
    // TODO: Persist chunks + embeddings to pgvector table.
  }

  @Override
  public List<String> query(float[] queryVector, int topK) {
    return Collections.emptyList();
  }
}
