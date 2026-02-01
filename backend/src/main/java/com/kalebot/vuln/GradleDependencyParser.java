package com.kalebot.vuln;

import com.kalebot.model.vuln.Dependency;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleDependencyParser {
  private static final Pattern DEP_PATTERN = Pattern.compile(
      "(?m)^\\s*(?:api|implementation|compileOnly|runtimeOnly|testImplementation|testCompileOnly|testRuntimeOnly|compile|runtimeOnly|annotationProcessor)\\s*\\(?['\"]([^'\"]+)['\"]\\)?"
  );

  public List<Dependency> parse(String content) {
    if (content == null || content.isBlank()) {
      return List.of();
    }
    Matcher matcher = DEP_PATTERN.matcher(content);
    List<Dependency> dependencies = new ArrayList<>();
    while (matcher.find()) {
      String coords = matcher.group(1);
      String[] parts = coords.split(":");
      if (parts.length < 3) {
        continue;
      }
      String group = parts[0];
      String artifact = parts[1];
      String version = parts[2];
      dependencies.add(new Dependency(group, artifact, version, buildPurl(group, artifact, version)));
    }
    return dependencies;
  }

  private String buildPurl(String groupId, String artifactId, String version) {
    if (version == null || version.isBlank()) {
      return "pkg:maven/" + groupId + "/" + artifactId;
    }
    return "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
  }
}
