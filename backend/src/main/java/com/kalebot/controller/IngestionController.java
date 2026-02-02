package com.kalebot.controller;

import com.kalebot.core.IngestionService;
import com.kalebot.model.IngestTextRequest;
import com.kalebot.model.IngestUrlRequest;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {
  private final IngestionService ingestionService;

  public IngestionController(IngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping("/url")
  public Map<String, Boolean> ingestUrl(@Valid @RequestBody IngestUrlRequest request) {
    ingestionService.ingestUrl(request.url());
    return Map.of("accepted", true);
  }

  @PostMapping("/text")
  public Map<String, Boolean> ingestText(@Valid @RequestBody IngestTextRequest request) {
    ingestionService.ingestText(request.sourceId(), request.text());
    return Map.of("accepted", true);
  }
}
