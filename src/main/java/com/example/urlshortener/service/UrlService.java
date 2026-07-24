package com.example.urlshortener.service;

import com.example.urlshortener.api.AnalyticsResponse;
import com.example.urlshortener.api.CreateUrlRequest;
import com.example.urlshortener.api.CreateUrlResponse;
import com.example.urlshortener.domain.AliasGenerator;
import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.domain.UrlPolicy;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.metrics.UrlMetrics;
import com.example.urlshortener.repository.UrlMappingRepository;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class UrlService {

    private final UrlMappingRepository repository;
    private final UrlMappingWriter writer;
    private final AnalyticsWriter analyticsWriter;
    private final AliasGenerator aliasGenerator;
    private final UrlPolicy urlPolicy;
    private final UrlMetrics metrics;
    private final Clock clock;
    private final String baseUrl;
    private final int maxRetries;

    public UrlService(
            UrlMappingRepository repository,
            UrlMappingWriter writer,
            AnalyticsWriter analyticsWriter,
            AliasGenerator aliasGenerator,
            UrlPolicy urlPolicy,
            UrlMetrics metrics,
            Clock clock,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.alias.max-retries:3}") int maxRetries) {
        this.repository = repository;
        this.writer = writer;
        this.analyticsWriter = analyticsWriter;
        this.aliasGenerator = aliasGenerator;
        this.urlPolicy = urlPolicy;
        this.metrics = metrics;
        this.clock = clock;
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
    }

    @Transactional
    public CreateUrlResponse createUrl(CreateUrlRequest request) {
        Instant now = Instant.now(clock);

        urlPolicy.validateDestinationUrl(request.destinationUrl());
        urlPolicy.validateExpiration(request.expiresAt(), now);

        UrlMapping saved;
        if (request.customAlias() != null && !request.customAlias().isBlank()) {
            urlPolicy.validateAlias(request.customAlias());
            UrlMapping mapping = new UrlMapping(request.customAlias(), request.destinationUrl(), request.expiresAt(), now);
            saved = writer.save(mapping, true);
        } else {
            saved = createWithGeneratedAlias(request.destinationUrl(), request.expiresAt(), now);
        }

        metrics.incrementUrlCreation();
        return toResponse(saved);
    }

    private UrlMapping createWithGeneratedAlias(String destinationUrl, Instant expiresAt, Instant now) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String alias = aliasGenerator.generate();
            UrlMapping mapping = new UrlMapping(alias, destinationUrl, expiresAt, now);
            try {
                return writer.save(mapping, false);
            } catch (DataIntegrityViolationException e) {
                // collision on generated alias — retry
            }
        }
        metrics.incrementUrlCreationError("ALIAS_EXHAUSTED");
        throw new IllegalStateException("Could not generate a unique alias after " + maxRetries + " attempts.");
    }

    public String resolveRedirect(String shortCode) {
        Timer.Sample sample = metrics.startRedirectTimer();
        try {
            UrlMapping mapping = repository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));

            if (mapping.isExpired(Instant.now(clock))) {
                metrics.incrementRedirectError("EXPIRED");
                throw new UrlExpiredException(shortCode);
            }

            try {
                analyticsWriter.recordClick(mapping.getId());
            } catch (Exception e) {
                // best-effort: analytics failure must not block the redirect
            }

            return mapping.getDestinationUrl();
        } finally {
            metrics.stopRedirectTimer(sample);
        }
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return new AnalyticsResponse(mapping.getShortCode(), mapping.getClickCount());
    }

    private CreateUrlResponse toResponse(UrlMapping mapping) {
        return new CreateUrlResponse(
                mapping.getShortCode(),
                baseUrl + "/" + mapping.getShortCode(),
                mapping.getDestinationUrl(),
                mapping.getExpiresAt(),
                mapping.getCreatedAt()
        );
    }
}
