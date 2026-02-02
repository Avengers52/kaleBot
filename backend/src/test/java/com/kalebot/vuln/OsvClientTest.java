package com.kalebot.vuln;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalebot.model.vuln.Dependency;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class OsvClientTest {
  private MockWebServer server;
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    jdbcTemplate = new JdbcTemplate(new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .build());
    jdbcTemplate.execute("CREATE TABLE osv_vuln_cache (osv_id TEXT PRIMARY KEY, jsonb TEXT, fetched_at TIMESTAMP)");
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void queriesBatchAndHydratesVulns() throws InterruptedException {
    server.enqueue(new MockResponse()
        .setBody("{\"results\":[{\"vulns\":[{\"id\":\"OSV-1\"}]},{\"vulns\":[]}]}")
        .addHeader("Content-Type", "application/json"));
    server.enqueue(new MockResponse()
        .setBody("{\"id\":\"OSV-1\",\"summary\":\"Test\",\"details\":\"Details\",\"aliases\":[\"CVE-2024-0001\"],\"references\":[{\"type\":\"ADVISORY\",\"url\":\"https://example.com\"}]}")
        .addHeader("Content-Type", "application/json"));

    WebClient webClient = WebClient.builder().baseUrl(server.url("/").toString()).build();
    OsvClient client = new OsvClient(webClient, jdbcTemplate, new ObjectMapper());
    List<Dependency> dependencies = List.of(
        new Dependency("com.acme", "demo", "1.0.0", "pkg:maven/com.acme/demo@1.0.0"),
        new Dependency("com.acme", "noop", "2.0.0", "pkg:maven/com.acme/noop@2.0.0")
    );

    StepVerifier.create(client.queryBatch(OsvClient.DependencyCoordinate.from(dependencies))
            .flatMap(results -> client.fetchVulnerabilities(results.get(0).ids())))
        .assertNext(vulns -> {
          assertThat(vulns).hasSize(1);
          assertThat(vulns.get(0).id()).isEqualTo("OSV-1");
        })
        .verifyComplete();

    RecordedRequest batchRequest = server.takeRequest();
    assertThat(batchRequest.getPath()).isEqualTo("/v1/querybatch");
    assertThat(batchRequest.getBody().readUtf8()).contains("com.acme:demo");

    RecordedRequest vulnRequest = server.takeRequest();
    assertThat(vulnRequest.getPath()).isEqualTo("/v1/vulns/OSV-1");
  }
}
