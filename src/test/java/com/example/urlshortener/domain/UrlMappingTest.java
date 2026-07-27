package com.example.urlshortener.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UrlMappingTest {

    private static final Instant NOW = Instant.parse("2024-06-01T12:00:00Z");

    @Test
    void isNotExpiredWhenExpiresAtIsNull() {
        UrlMapping mapping = new UrlMapping("abc", "https://example.com", null, NOW);
        assertThat(mapping.isExpired(NOW)).isFalse();
    }

    @Test
    void isNotExpiredWhenExpiresAtIsInTheFuture() {
        Instant future = NOW.plus(1, ChronoUnit.HOURS);
        UrlMapping mapping = new UrlMapping("abc", "https://example.com", future, NOW);
        assertThat(mapping.isExpired(NOW)).isFalse();
    }

    @Test
    void isExpiredWhenExpiresAtEqualsNow() {
        UrlMapping mapping = new UrlMapping("abc", "https://example.com", NOW, NOW);
        assertThat(mapping.isExpired(NOW)).isTrue();
    }

    @Test
    void isExpiredWhenExpiresAtIsInThePast() {
        Instant past = NOW.minus(1, ChronoUnit.SECONDS);
        UrlMapping mapping = new UrlMapping("abc", "https://example.com", past, NOW);
        assertThat(mapping.isExpired(NOW)).isTrue();
    }
}
