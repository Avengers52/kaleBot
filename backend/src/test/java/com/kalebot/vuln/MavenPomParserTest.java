package com.kalebot.vuln;

import com.kalebot.model.vuln.Dependency;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MavenPomParserTest {
  private final MavenPomParser parser = new MavenPomParser();

  @Test
  void resolvesPropertiesAndDependencyManagement() {
    String pom = """
        <project>
          <properties>
            <spring.version>5.0.0</spring.version>
            <slf4j.version>1.7.30</slf4j.version>
          </properties>
          <dependencyManagement>
            <dependencies>
              <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
                <version>${slf4j.version}</version>
              </dependency>
            </dependencies>
          </dependencyManagement>
          <dependencies>
            <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-core</artifactId>
              <version>${spring.version}</version>
            </dependency>
            <dependency>
              <groupId>org.slf4j</groupId>
              <artifactId>slf4j-api</artifactId>
            </dependency>
          </dependencies>
        </project>
        """;

    List<Dependency> dependencies = parser.parse(pom);

    assertThat(dependencies).extracting(Dependency::group)
        .containsExactly("org.springframework", "org.slf4j");
    assertThat(dependencies).extracting(Dependency::version)
        .containsExactly("5.0.0", "1.7.30");
  }
}
