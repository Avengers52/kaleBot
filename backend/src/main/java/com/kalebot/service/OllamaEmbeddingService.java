package com.kalebot.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kalebot.core.EmbeddingService;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class OllamaEmbeddingService implements EmbeddingService {
  private final WebClient webClient;
  private final String model;
  private final String baseUrl;

  public OllamaEmbeddingService(WebClient webClient, String model, String baseUrl) {
    this.webClient = webClient;
    this.model = model;
    this.baseUrl = baseUrl;
  }

  @Override
  public float[] embed(String text) {
    EmbeddingResponse response = webClient.post()
        .uri("/api/embed")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new EmbeddingRequest(model, text))
        .retrieve()
        .bodyToMono(EmbeddingResponse.class)
        .timeout(Duration.ofSeconds(30))
        .onErrorMap(ex -> new IllegalStateException(
            "Failed to fetch embeddings from Ollama at " + baseUrl + ". Is it running?",
            ex))
        .block();
    if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
      throw new IllegalStateException("Ollama embedding response was empty.");
    }
    List<Double> embedding = response.embeddings().get(0);
    float[] vector = new float[embedding.size()];
    for (int i = 0; i < embedding.size(); i++) {
      vector[i] = embedding.get(i).floatValue();
    }
    return vector;
  }

  private record EmbeddingRequest(
      @JsonProperty("model") String model,
      @JsonProperty("input") String input
  ) {
  }

  private record EmbeddingResponse(@JsonProperty("embeddings") List<List<Double>> embeddings) {
  }
}
