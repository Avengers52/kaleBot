package com.kalebot.service;

import com.kalebot.core.ModelClient;
import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Flux;

public class MockModelClient implements ModelClient {
  @Override
  public Flux<String> streamGenerate(String prompt) {
    List<String> tokens = List.of(
        "This ",
        "is ",
        "a ",
        "mock ",
        "response.");
    return Flux.fromIterable(tokens)
        .delayElements(Duration.ofMillis(200));
  }
}
