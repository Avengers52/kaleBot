package com.kalebot.core;

import com.kalebot.model.EmbeddedChunk;
import com.kalebot.model.SourceChunk;
import java.util.List;

public interface VectorStoreService {
  void upsert(long docId, List<EmbeddedChunk> chunks);

  List<SourceChunk> query(float[] queryVector, int topK);
}
