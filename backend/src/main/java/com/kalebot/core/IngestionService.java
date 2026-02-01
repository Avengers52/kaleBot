package com.kalebot.core;

public interface IngestionService {
  void ingestUrl(String url);

  void ingestText(String sourceId, String text);
}
