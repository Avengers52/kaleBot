package com.kalebot.service;

import com.kalebot.core.EmbeddingService;
import java.util.Random;

public class SimpleEmbeddingService implements EmbeddingService {
  private final int dimensions;
  private final Random random = new Random(42);

  public SimpleEmbeddingService(int dimensions) {
    this.dimensions = dimensions;
  }

  @Override
  public float[] embed(String text) {
    float[] vector = new float[dimensions];
    for (int i = 0; i < dimensions; i++) {
      vector[i] = random.nextFloat();
    }
    return vector;
  }
}
