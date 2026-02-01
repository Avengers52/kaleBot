package com.kalebot.config;

import com.kalebot.core.EmbeddingService;
import com.kalebot.service.OllamaEmbeddingService;
import com.kalebot.service.SimpleEmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EmbeddingConfig {
  @Bean
  public EmbeddingService embeddingService(
      WebClient.Builder builder,
      @Value("${embedding.provider:mock}") String provider,
      @Value("${embedding.ollama.base-url:http://localhost:11434}") String baseUrl,
      @Value("${embedding.ollama.model:mxbai-embed-large}") String modelName,
      @Value("${embedding.dimensions:1536}") int dimensions
  ) {
    if ("ollama".equalsIgnoreCase(provider)) {
      WebClient webClient = builder.baseUrl(baseUrl).build();
      return new OllamaEmbeddingService(webClient, modelName, baseUrl);
    }
    return new SimpleEmbeddingService(dimensions);
  }
}
