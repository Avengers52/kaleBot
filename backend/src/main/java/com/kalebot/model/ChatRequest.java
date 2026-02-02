package com.kalebot.model;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "message is required") String message) {
}
