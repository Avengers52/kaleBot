package com.kalebot.vuln;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalebot.model.vuln.Reference;
import com.kalebot.model.vuln.Severity;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class OsvClient {
  private final WebClient webClient;
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public OsvClient(WebClient webClient, JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.webClient = webClient;
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  public Mono<List<OsvBatchResult>> queryBatch(List<DependencyCoordinate> coordinates) {
    if (coordinates.isEmpty()) {
      return Mono.just(List.of());
    }
    OsvBatchRequest request = new OsvBatchRequest(
        coordinates.stream()
            .map(dep -> new OsvQuery(new OsvPackage("Maven", dep.group() + ":" + dep.artifact()), dep.version()))
            .toList()
    );
    return webClient.post()
        .uri("/v1/querybatch")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(OsvBatchResponse.class)
        .map(response -> Optional.ofNullable(response.results()).orElseGet(List::of));
  }

  public Mono<List<OsvVulnerability>> fetchVulnerabilities(List<String> ids) {
    if (ids.isEmpty()) {
      return Mono.just(List.of());
    }
    return Flux.fromIterable(ids)
        .flatMap(this::fetchVulnerability, 5)
        .collectList();
  }

  private Mono<OsvVulnerability> fetchVulnerability(String id) {
    return Mono.fromCallable(() -> loadFromCache(id))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(optional -> optional
            .map(Mono::just)
            .orElseGet(() -> webClient.get()
                .uri("/v1/vulns/{id}", id)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(node -> Mono.fromCallable(() -> saveToCache(id, node))
                    .subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(node))))
        .map(this::toVulnerability);
  }

  private Optional<JsonNode> loadFromCache(String id) {
    List<String> rows = jdbcTemplate.query(
        "SELECT jsonb FROM osv_vuln_cache WHERE osv_id = ?",
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
          "INSERT INTO osv_vuln_cache (osv_id, jsonb, fetched_at) VALUES (?, ?, ?) "
              + "ON CONFLICT (osv_id) DO UPDATE SET jsonb = EXCLUDED.jsonb, fetched_at = EXCLUDED.fetched_at",
          id,
          objectMapper.writeValueAsString(node),
          OffsetDateTime.now()
      );
    } catch (Exception ignored) {
      // Best-effort cache.
    }
    return null;
  }

  private OsvVulnerability toVulnerability(JsonNode node) {
    String id = node.path("id").asText();
    List<String> aliases = new ArrayList<>();
    node.path("aliases").forEach(alias -> aliases.add(alias.asText()));
    String summary = node.path("summary").asText("");
    String details = node.path("details").asText("");
    List<Reference> references = new ArrayList<>();
    node.path("references").forEach(ref -> references.add(
        new Reference(ref.path("type").asText(""), ref.path("url").asText(""))
    ));
    Severity severity = parseSeverity(node);
    return new OsvVulnerability(id, aliases, summary, details, severity, references);
  }

  private Severity parseSeverity(JsonNode node) {
    JsonNode severityNode = node.path("severity");
    if (severityNode.isArray() && severityNode.size() > 0) {
      JsonNode first = severityNode.get(0);
      String vector = first.path("score").asText("");
      return new Severity(0.0, "", vector, "OSV");
    }
    return new Severity(0.0, "", "", "UNKNOWN");
  }

  public record DependencyCoordinate(String group, String artifact, String version) {
    public static List<DependencyCoordinate> from(List<com.kalebot.model.vuln.Dependency> dependencies) {
      return dependencies.stream()
          .filter(dep -> dep.version() != null && !dep.version().isBlank())
          .map(dep -> new DependencyCoordinate(dep.group(), dep.artifact(), dep.version()))
          .collect(Collectors.toList());
    }
  }

  private record OsvBatchRequest(List<OsvQuery> queries) {
  }

  private record OsvQuery(@com.fasterxml.jackson.annotation.JsonProperty("package") OsvPackage packageInfo, String version) {
  }

  private record OsvPackage(String ecosystem, String name) {
  }

  private record OsvBatchResponse(List<OsvBatchResult> results) {
  }

  public record OsvBatchResult(List<OsvBatchVuln> vulns) {
    public List<String> ids() {
      if (vulns == null) {
        return List.of();
      }
      return vulns.stream().map(OsvBatchVuln::id).toList();
    }
  }

  private record OsvBatchVuln(String id) {
  }

  public record OsvVulnerability(
      String id,
      List<String> aliases,
      String summary,
      String details,
      Severity severity,
      List<Reference> references
  ) {
  }
}
