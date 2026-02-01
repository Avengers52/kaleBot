package com.kalebot.model.vuln;

import java.util.List;

public record Finding(Dependency dependency, List<Vulnerability> vulnerabilities) {
}
