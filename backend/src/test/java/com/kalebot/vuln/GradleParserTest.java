package com.kalebot.vuln;

import com.kalebot.model.vuln.Dependency;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradleParserTest {
  private final GradleDependencyParser parser = new GradleDependencyParser();

  @Test
  void parsesCommonDependencyNotations() {
    String gradle = """
        dependencies {
          implementation("com.squareup.okhttp3:okhttp:4.11.0")
          testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
          compileOnly "org.projectlombok:lombok:1.18.30"
        }
        """;

    List<Dependency> dependencies = parser.parse(gradle);

    assertThat(dependencies).extracting(Dependency::artifact)
        .containsExactly("okhttp", "junit-jupiter", "lombok");
    assertThat(dependencies).extracting(Dependency::version)
        .containsExactly("4.11.0", "5.10.2", "1.18.30");
  }
}
