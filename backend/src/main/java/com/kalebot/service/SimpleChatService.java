package com.kalebot.service;

import com.kalebot.core.ChatService;
import com.kalebot.core.ModelClient;
import com.kalebot.core.RagService;
import com.kalebot.model.ChatChunk;
import com.kalebot.model.ChatRequest;
import com.kalebot.model.SourceChunk;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SimpleChatService implements ChatService {
  private final ModelClient modelClient;
  private final RagService ragService;

  public SimpleChatService(ModelClient modelClient, RagService ragService) {
    this.modelClient = modelClient;
    this.ragService = ragService;
  }

  @Override
  public Flux<ChatChunk> streamChat(ChatRequest request) {
    List<SourceChunk> context = ragService.retrieveContext(request.message());
    StringBuilder promptBuilder = new StringBuilder();
    promptBuilder.append("User: ").append(request.message()).append("\n");
    if (!context.isEmpty()) {
      promptBuilder.append("Context:\n");
      for (SourceChunk chunk : context) {
        promptBuilder.append("- ").append(chunk.content()).append("\n");
      }
    }
    promptBuilder.append("Assistant:");

    return modelClient.streamGenerate(promptBuilder.toString())
        .map(ChatChunk::new);
  }
}
