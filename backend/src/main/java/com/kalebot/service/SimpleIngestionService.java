package com.kalebot.service;

import com.kalebot.core.IngestionService;
import com.kalebot.core.EmbeddingService;
import com.kalebot.core.VectorStoreService;
import com.kalebot.model.EmbeddedChunk;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@ConditionalOnProperty(name = "vectorstore.enabled", havingValue = "true")
public class SimpleIngestionService implements IngestionService {
  private static final Logger logger = LoggerFactory.getLogger(SimpleIngestionService.class);
  private final JdbcTemplate jdbcTemplate;
  private final EmbeddingService embeddingService;
  private final VectorStoreService vectorStoreService;
  private final WebClient webClient;
  private final Chunker chunker;

  public SimpleIngestionService(
      JdbcTemplate jdbcTemplate,
      EmbeddingService embeddingService,
      VectorStoreService vectorStoreService,
      WebClient.Builder webClientBuilder,
      @Value("${ingestion.chunk-size:1000}") int chunkSize,
      @Value("${ingestion.chunk-overlap:200}") int chunkOverlap
  ) {
    this.jdbcTemplate = jdbcTemplate;
    this.embeddingService = embeddingService;
    this.vectorStoreService = vectorStoreService;
    this.webClient = webClientBuilder.build();
    this.chunker = new Chunker(chunkSize, chunkOverlap);
  }

  @Override
  public void ingestUrl(String url) {
    logger.info("Received URL ingestion request: {}", url);
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("url is required");
    }
    URI uri = URI.create(url);
    if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
      throw new IllegalArgumentException("Only http/https URLs are supported");
    }
    validateHost(uri.getHost());
    FetchedDocument fetched = fetch(uri);
    String text = extractText(fetched);
    ingestContent(uri.toString(), text);
  }

  @Override
  public void ingestText(String sourceId, String text) {
    if (sourceId == null || sourceId.isBlank()) {
      throw new IllegalArgumentException("sourceId is required");
    }
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("text is required");
    }
    ingestContent(sourceId, text);
  }

  private void ingestContent(String sourceId, String text) {
    long docId = insertDocument(sourceId, text);
    List<String> chunks = chunker.chunk(text);
    List<EmbeddedChunk> embeddedChunks = new ArrayList<>();
    for (String chunk : chunks) {
      embeddedChunks.add(new EmbeddedChunk(chunk, embeddingService.embed(chunk)));
    }
    vectorStoreService.upsert(docId, embeddedChunks);
    logger.info("Stored {} chunks for sourceId={}", embeddedChunks.size(), sourceId);
  }

  private long insertDocument(String sourceId, String text) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      var statement = connection.prepareStatement(
          "INSERT INTO ingested_docs (source_id, content, created_at) VALUES (?, ?, ?)",
          new String[] {"id"}
      );
      statement.setString(1, sourceId);
      statement.setString(2, text);
      statement.setObject(3, OffsetDateTime.now());
      return statement;
    }, keyHolder);
    if (keyHolder.getKey() == null) {
      throw new IllegalStateException("Failed to create ingested document record.");
    }
    return keyHolder.getKey().longValue();
  }

  private void validateHost(String host) {
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("URL host is required");
    }
    if ("localhost".equalsIgnoreCase(host)) {
      throw new IllegalArgumentException("Localhost URLs are not allowed");
    }
    try {
      InetAddress address = InetAddress.getByName(host);
      if (address.isAnyLocalAddress()
          || address.isLoopbackAddress()
          || address.isSiteLocalAddress()
          || address.isLinkLocalAddress()) {
        throw new IllegalArgumentException("Private network URLs are not allowed");
      }
    } catch (Exception ex) {
      throw new IllegalArgumentException("Unable to resolve URL host", ex);
    }
  }

  private FetchedDocument fetch(URI uri) {
    return webClient.get()
        .uri(uri)
        .accept(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN)
        .exchangeToMono(response -> response.bodyToMono(String.class)
            .map(body -> new FetchedDocument(body, response.headers().contentType())))
        .timeout(Duration.ofSeconds(15))
        .blockOptional()
        .orElseThrow(() -> new IllegalStateException("No response body from URL"));
  }

  private String extractText(FetchedDocument document) {
    Optional<MediaType> mediaType = document.contentType();
    String body = document.body();
    if (mediaType.isPresent() && MediaType.TEXT_HTML.isCompatibleWith(mediaType.get())) {
      return org.jsoup.Jsoup.parse(body).text();
    }
    return body;
  }

  private record FetchedDocument(String body, Optional<MediaType> contentType) {
  }
}
