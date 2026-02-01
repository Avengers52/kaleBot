package com.kalebot.core;

import com.kalebot.model.ChatChunk;
import com.kalebot.model.ChatRequest;
import reactor.core.publisher.Flux;

public interface ChatService {
  Flux<ChatChunk> streamChat(ChatRequest request);
}
