package com.kalebot.service;

import com.kalebot.core.ChatService;
import com.kalebot.core.ModelClient;
import com.kalebot.core.RagService;
import com.kalebot.core.VulnerabilityScanService;
import com.kalebot.model.ChatDelta;
import com.kalebot.model.ChatFinal;
import com.kalebot.model.ChatRequest;
import com.kalebot.model.ChatStreamEvent;
import com.kalebot.model.SourceChunk;
import com.kalebot.model.vuln.Dependency;
import com.kalebot.model.vuln.Finding;
import com.kalebot.model.vuln.ScanReport;
import com.kalebot.model.vuln.ScanSummary;
import com.kalebot.model.vuln.Source;
import com.kalebot.model.vuln.Vulnerability;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SimpleChatService implements ChatService {
  private final ModelClient modelClient;
  private final RagService ragService;
  private final VulnerabilityScanService vulnerabilityScanService;

  public SimpleChatService(
      ModelClient modelClient,
      RagService ragService,
      VulnerabilityScanService vulnerabilityScanService
  ) {
    this.modelClient = modelClient;
    this.ragService = ragService;
    this.vulnerabilityScanService = vulnerabilityScanService;
  }

  @Override
  public Flux<ChatStreamEvent> streamChat(ChatRequest request) {
    String message = request.message();
    return maybeScan(message)
        .flatMap(scanReport -> Mono.just(buildScanPrompt(message, scanReport)))
        .switchIfEmpty(buildRagPrompt(message))
        .flatMapMany(promptContext -> {
          Flux<ChatStreamEvent> deltas = modelClient.streamGenerate(promptContext.prompt())
              .map(token -> new ChatStreamEvent("delta", new ChatDelta(token)));
          return deltas.concatWith(Mono.just(new ChatStreamEvent("final", promptContext.finalPayload())));
        });
  }

  private Mono<ScanReport> maybeScan(String message) {
    if (looksLikeMavenPom(message)) {
      return vulnerabilityScanService.scan("maven_pom", message);
    }
    if (looksLikeGradle(message)) {
      return vulnerabilityScanService.scan("gradle", message);
    }
    return Mono.empty();
  }

  private boolean looksLikeMavenPom(String message) {
    String lower = message.toLowerCase(Locale.ROOT);
    return lower.contains("<project") && lower.contains("<dependencies");
  }

  private boolean looksLikeGradle(String message) {
    String lower = message.toLowerCase(Locale.ROOT);
    return lower.contains("implementation(")
        || lower.contains("implementation \"")
        || lower.contains("implementation '")
        || lower.contains("testimplementation")
        || lower.contains("compileonly")
        || lower.contains("runtimeonly")
        || lower.contains("api(");
  }

  private Mono<PromptContext> buildRagPrompt(String message) {
    return Mono.fromCallable(() -> ragService.retrieveContext(message))
        .subscribeOn(Schedulers.boundedElastic())
        .map(context -> {
          StringBuilder promptBuilder = new StringBuilder();
          promptBuilder.append("User: ").append(message).append("\n");
          if (!context.isEmpty()) {
            promptBuilder.append("Context:\n");
            for (SourceChunk chunk : context) {
              promptBuilder.append("- ").append(chunk.content()).append("\n");
            }
          }
          promptBuilder.append("Assistant:");
          return new PromptContext(promptBuilder.toString(), new ChatFinal(List.of(), null));
        });
  }

  private PromptContext buildScanPrompt(String message, ScanReport scanReport) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are Vulnerability Copilot. ")
        .append("Analyze the dependency list and vulnerability findings. ")
        .append("Provide actionable remediation steps and cite sources with [1], [2], etc. ")
        .append("Sources are listed at the end in order.\n\n");
    prompt.append("Dependencies:\n");
    for (Dependency dependency : scanReport.dependencies()) {
      prompt.append("- ")
          .append(dependency.group())
          .append(":")
          .append(dependency.artifact());
      if (dependency.version() != null && !dependency.version().isBlank()) {
        prompt.append(":").append(dependency.version());
      }
      prompt.append("\n");
    }
    if (!scanReport.findings().isEmpty()) {
      prompt.append("\nFindings:\n");
      for (Finding finding : scanReport.findings()) {
        prompt.append("Dependency: ")
            .append(finding.dependency().group())
            .append(":")
            .append(finding.dependency().artifact())
            .append("\n");
        for (Vulnerability vulnerability : finding.vulnerabilities()) {
          prompt.append("- ")
              .append(vulnerability.osvId())
              .append(": ")
              .append(vulnerability.summary())
              .append("\n");
          if (vulnerability.severity() != null && vulnerability.severity().cvssScore() > 0) {
            prompt.append("  Severity: ")
                .append(vulnerability.severity().cvssSeverity())
                .append(" (")
                .append(vulnerability.severity().cvssScore())
                .append(")\n");
          }
          if (vulnerability.details() != null && !vulnerability.details().isBlank()) {
            prompt.append("  Details: ").append(vulnerability.details()).append("\n");
          }
        }
      }
    } else {
      prompt.append("\nNo known vulnerabilities were found for the provided dependencies.\n");
    }
    if (!scanReport.sources().isEmpty()) {
      prompt.append("\nSources:\n");
      for (int i = 0; i < scanReport.sources().size(); i++) {
        Source source = scanReport.sources().get(i);
        prompt.append("[").append(i + 1).append("] ")
            .append(source.title())
            .append(" - ")
            .append(source.url())
            .append("\n");
      }
    }
    prompt.append("\nAssistant:");
    List<Source> sources = scanReport.sources();
    ScanSummary summary = new ScanSummary(scanReport.dependencies().size(), scanReport.findings().size());
    ChatFinal finalPayload = new ChatFinal(sources, summary);
    return new PromptContext(prompt.toString(), finalPayload);
  }

  private record PromptContext(String prompt, ChatFinal finalPayload) {
  }
}
