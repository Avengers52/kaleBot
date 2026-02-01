package com.kalebot.controller;

import com.kalebot.core.ChatService;
import com.kalebot.model.ChatChunk;
import com.kalebot.model.ChatRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<ChatChunk>> streamChat(@RequestBody ChatRequest request) {
    return chatService.streamChat(request)
        .map(chunk -> ServerSentEvent.builder(chunk)
            .event("chunk")
            .build());
  }
}
