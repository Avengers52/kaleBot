package com.kalebot.service;

import com.kalebot.core.EmbeddingService;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class SimpleEmbeddingService implements EmbeddingService {
  private static final int DIMENSIONS = 1536;
  private final Random random = new Random(42);

  @Override
  public float[] embed(String text) {
    float[] vector = new float[DIMENSIONS];
    for (int i = 0; i < DIMENSIONS; i++) {
      vector[i] = random.nextFloat();
    }
    return vector;
  }
}
