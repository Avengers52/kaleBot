package com.kalebot.service;

import java.util.ArrayList;
import java.util.List;

public class Chunker {
  private final int maxChars;
  private final int overlap;

  public Chunker(int maxChars, int overlap) {
    if (maxChars <= 0) {
      throw new IllegalArgumentException("maxChars must be positive");
    }
    if (overlap < 0 || overlap >= maxChars) {
      throw new IllegalArgumentException("overlap must be between 0 and maxChars");
    }
    this.maxChars = maxChars;
    this.overlap = overlap;
  }

  public List<String> chunk(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    String normalized = text.strip();
    List<String> chunks = new ArrayList<>();
    int start = 0;
    while (start < normalized.length()) {
      int end = Math.min(start + maxChars, normalized.length());
      String chunk = normalized.substring(start, end).strip();
      if (!chunk.isBlank()) {
        chunks.add(chunk);
      }
      if (end >= normalized.length()) {
        break;
      }
      start = Math.max(0, end - overlap);
    }
    return chunks;
  }
}
