package com.kalebot.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kalebot.core.ModelClient;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class OllamaModelClient implements ModelClient {
  private final WebClient webClient;
  private final String model;
  private final String baseUrl;

  public OllamaModelClient(WebClient webClient, String model, String baseUrl) {
    this.webClient = webClient;
    this.model = model;
    this.baseUrl = baseUrl;
  }

  @Override
  public Flux<String> streamGenerate(String prompt) {
    Map<String, Object> payload = Map.of(
        "model", model,
        "prompt", prompt,
        "stream", true
    );

    return webClient.post()
        .uri("/api/generate")
        .accept(MediaType.APPLICATION_NDJSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .retrieve()
        .bodyToFlux(OllamaResponse.class)
        .map(OllamaResponse::response)
        .onErrorMap(ex -> new IllegalStateException(
            "Failed to stream from Ollama at " + baseUrl + ". Is it running?",
            ex));
  }

  private record OllamaResponse(@JsonProperty("response") String response) {
  }
}
