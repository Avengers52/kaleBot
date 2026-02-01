package com.kalebot.core;

import com.kalebot.model.SourceChunk;
import java.util.List;

public interface RagService {
  List<SourceChunk> retrieveContext(String query);
}
