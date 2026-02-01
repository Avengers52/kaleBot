package com.kalebot.core;

import java.util.List;

public interface VectorStoreService {
  void upsert(List<String> chunks);

  List<String> query(float[] queryVector, int topK);
}
