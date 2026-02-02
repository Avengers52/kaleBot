package com.kalebot.model;

import jakarta.validation.constraints.NotBlank;

public record IngestUrlRequest(@NotBlank(message = "url is required") String url) {
}
