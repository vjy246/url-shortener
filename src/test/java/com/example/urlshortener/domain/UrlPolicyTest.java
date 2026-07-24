package com.example.urlshortener.domain;

import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ReservedAliasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlPolicyTest {

    private final UrlPolicy policy = new UrlPolicy();
    private static final Instant NOW = Instant.parse("2024-06-01T12:00:00Z");

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com", "http://example.com/path?q=1"})
    void acceptsValidUrls(String url) {
        assertThatCode(() -> policy.validateDestinationUrl(url)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com", "javascript:alert(1)", "file:///etc/passwd", "data:text/html,hi"})
    void rejectsNonHttpSchemes(String url) {
        assertThatThrownBy(() -> policy.validateDestinationUrl(url))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsUrlWithCredentials() {
        assertThatThrownBy(() -> policy.validateDestinationUrl("http://user" + ":pass@example.com"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsBlankUrl() {
        assertThatThrownBy(() -> policy.validateDestinationUrl("  "))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void acceptsValidAlias() {
        assertThatCode(() -> policy.validateAlias("my-alias_1")).doesNotThrowAnyException();
    }

    @Test
    void rejectsAliasTooShort() {
        assertThatThrownBy(() -> policy.validateAlias("ab"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsReservedAlias() {
        assertThatThrownBy(() -> policy.validateAlias("api"))
                .isInstanceOf(ReservedAliasException.class);
        assertThatThrownBy(() -> policy.validateAlias("actuator"))
                .isInstanceOf(ReservedAliasException.class);
    }

    @Test
    void rejectsAliasWithInvalidCharacters() {
        assertThatThrownBy(() -> policy.validateAlias("bad alias!"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void acceptsExpirationInTheFuture() {
        assertThatCode(() -> policy.validateExpiration(NOW.plus(1, ChronoUnit.HOURS), NOW))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPastExpiration() {
        assertThatThrownBy(() -> policy.validateExpiration(NOW.minus(1, ChronoUnit.SECONDS), NOW))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsExpirationAtNow() {
        assertThatThrownBy(() -> policy.validateExpiration(NOW, NOW))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void acceptsNullExpiration() {
        assertThatCode(() -> policy.validateExpiration(null, NOW)).doesNotThrowAnyException();
    }
}
