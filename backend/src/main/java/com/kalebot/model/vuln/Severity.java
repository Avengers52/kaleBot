package com.kalebot.model.vuln;

public record Severity(double cvssScore, String cvssSeverity, String vector, String source) {
}
