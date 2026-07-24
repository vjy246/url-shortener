package com.example.urlshortener.domain;

import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ReservedAliasException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class UrlPolicy {

    private static final Set<String> RESERVED_ALIASES = Set.of(
            "api", "actuator", "swagger-ui", "swagger-ui.html", "v3", "health", "info", "metrics", "prometheus"
    );

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,32}$");

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public void validateDestinationUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("Destination URL must not be blank.");
        }
        if (url.length() > 2048) {
            throw new InvalidUrlException("Destination URL exceeds maximum length of 2048 characters.");
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Destination URL is not a valid URI.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException("Destination URL must use http or https scheme.");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("Destination URL must contain a valid host.");
        }
        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            throw new InvalidUrlException("Destination URL must not contain credentials.");
        }
    }

    public void validateAlias(String alias) {
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new InvalidUrlException(
                    "Alias must be 3-32 characters and may only contain letters, digits, hyphens, and underscores.");
        }
        if (RESERVED_ALIASES.contains(alias.toLowerCase())) {
            throw new ReservedAliasException("Alias '" + alias + "' is reserved.");
        }
    }

    public void validateExpiration(Instant expiresAt, Instant now) {
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new InvalidUrlException("Expiration must be a future instant.");
        }
    }
}
