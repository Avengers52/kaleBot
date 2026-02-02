package com.kalebot.model;

import jakarta.validation.constraints.NotBlank;

public record IngestTextRequest(
    @NotBlank(message = "sourceId is required") String sourceId,
    @NotBlank(message = "text is required") String text
) {
}
