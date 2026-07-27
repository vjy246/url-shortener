package com.example.urlshortener.api;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CreateUrlRequest(
        @NotBlank(message = "destinationUrl must not be blank")
        String destinationUrl,
        String customAlias,
        Instant expiresAt
) {
}
