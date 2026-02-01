package com.kalebot.model;

import com.kalebot.model.vuln.ScanSummary;
import com.kalebot.model.vuln.Source;
import java.util.List;

public record ChatFinal(List<Source> sources, ScanSummary scan) {
}
