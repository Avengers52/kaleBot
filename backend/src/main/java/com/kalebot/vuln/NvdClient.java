package com.kalebot.vuln;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalebot.model.vuln.Severity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class NvdClient {
  private final WebClient webClient;
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;
  private final String apiKey;

  public NvdClient(WebClient webClient, JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, String apiKey) {
    this.webClient = webClient;
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
  }

  public Mono<Severity> fetchSeverity(String cveId) {
    return Mono.fromCallable(() -> loadFromCache(cveId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(optional -> optional
            .map(Mono::just)
            .orElseGet(() -> webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("cveId", cveId).build())
                .headers(headers -> {
                  if (apiKey != null && !apiKey.isBlank()) {
                    headers.add("apiKey", apiKey);
                  }
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(node -> Mono.fromCallable(() -> saveToCache(cveId, node))
                    .subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(node))))
        .map(this::extractSeverity)
        .flatMap(severity -> severity == null ? Mono.empty() : Mono.just(severity))
        .onErrorResume(error -> Mono.empty());
  }

  private Optional<JsonNode> loadFromCache(String id) {
    List<String> rows = jdbcTemplate.query(
        "SELECT jsonb FROM nvd_cve_cache WHERE cve_id = ?",
        (rs, rowNum) -> rs.getString("jsonb"),
        id
    );
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readTree(rows.get(0)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Void saveToCache(String id, JsonNode node) {
    try {
      jdbcTemplate.update(
          "INSERT INTO nvd_cve_cache (cve_id, jsonb, fetched_at) VALUES (?, ?, ?) "
              + "ON CONFLICT (cve_id) DO UPDATE SET jsonb = EXCLUDED.jsonb, fetched_at = EXCLUDED.fetched_at",
          id,
          objectMapper.writeValueAsString(node),
          OffsetDateTime.now()
      );
    } catch (Exception ignored) {
      // Best-effort cache.
    }
    return null;
  }

  private Severity extractSeverity(JsonNode node) {
    JsonNode vulnerabilities = node.path("vulnerabilities");
    if (!vulnerabilities.isArray() || vulnerabilities.isEmpty()) {
      return null;
    }
    JsonNode cve = vulnerabilities.get(0).path("cve");
    JsonNode metrics = cve.path("metrics");
    Severity v31 = extractCvss(metrics.path("cvssMetricV31"));
    if (v31 != null) {
      return v31;
    }
    Severity v40 = extractCvss(metrics.path("cvssMetricV40"));
    if (v40 != null) {
      return v40;
    }
    return null;
  }

  private Severity extractCvss(JsonNode metricsArray) {
    if (!metricsArray.isArray() || metricsArray.isEmpty()) {
      return null;
    }
    JsonNode cvssData = metricsArray.get(0).path("cvssData");
    double score = cvssData.path("baseScore").asDouble(0.0);
    String severity = cvssData.path("baseSeverity").asText("");
    String vector = cvssData.path("vectorString").asText("");
    return new Severity(score, severity, vector, "NVD");
  }
}
