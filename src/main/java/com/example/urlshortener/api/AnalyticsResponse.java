package com.example.urlshortener.api;

public record AnalyticsResponse(
        String shortCode,
        long clickCount
) {
}
