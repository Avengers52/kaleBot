package com.kalebot.core;

import com.kalebot.model.ChatStreamEvent;
import com.kalebot.model.ChatRequest;
import reactor.core.publisher.Flux;

public interface ChatService {
  Flux<ChatStreamEvent> streamChat(ChatRequest request);
}
