package com.example.urlshortener.api;

import java.time.Instant;

public record CreateUrlResponse(
        String shortCode,
        String shortUrl,
        String destinationUrl,
        Instant expiresAt,
        Instant createdAt
) {
}
