package com.kalebot.service;

import com.kalebot.core.RagService;
import com.kalebot.model.SourceChunk;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SimpleRagService implements RagService {
  @Override
  public List<SourceChunk> retrieveContext(String query) {
    return Collections.emptyList();
  }
}
