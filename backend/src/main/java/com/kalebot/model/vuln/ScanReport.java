package com.kalebot.model.vuln;

import java.util.List;

public record ScanReport(
    List<Dependency> dependencies,
    List<Finding> findings,
    List<Source> sources
) {
}
