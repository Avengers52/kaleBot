package com.kalebot.config;

import com.kalebot.core.ModelClient;
import com.kalebot.service.MockModelClient;
import com.kalebot.service.OllamaModelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ModelClientConfig {
  @Bean
  public ModelClient modelClient(
      WebClient.Builder builder,
      @Value("${model.provider:mock}") String provider,
      @Value("${model.ollama.base-url:http://localhost:11434}") String baseUrl,
      @Value("${model.ollama.model:llama3}") String modelName
  ) {
    if ("ollama".equalsIgnoreCase(provider)) {
      WebClient webClient = builder.baseUrl(baseUrl).build();
      return new OllamaModelClient(webClient, modelName, baseUrl);
    }
    return new MockModelClient();
  }
}
