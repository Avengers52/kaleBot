package com.kalebot.core;

import reactor.core.publisher.Flux;

public interface ModelClient {
  Flux<String> streamGenerate(String prompt);
}
