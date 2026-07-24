package com.example.urlshortener.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class UrlMetrics {

    private static final String URL_CREATION_TOTAL = "url.creation.total";
    private static final String URL_CREATION_ERROR_TOTAL = "url.creation.error.total";
    private static final String URL_REDIRECT_ERROR_TOTAL = "url.redirect.error.total";
    private static final String ANALYTICS_FAILURE_TOTAL = "url.analytics.failure.total";
    private static final String REDIRECT_LATENCY = "url.redirect.latency";

    private final Counter urlCreationCounter;
    private final MeterRegistry registry;

    public UrlMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.urlCreationCounter = Counter.builder(URL_CREATION_TOTAL)
                .description("Total number of short URLs created")
                .register(registry);
    }

    public void incrementUrlCreation() {
        urlCreationCounter.increment();
    }

    public void incrementUrlCreationError(String errorCode) {
        Counter.builder(URL_CREATION_ERROR_TOTAL)
                .tag("error", errorCode)
                .description("Total URL creation errors by error code")
                .register(registry)
                .increment();
    }

    public void incrementRedirectError(String errorCode) {
        Counter.builder(URL_REDIRECT_ERROR_TOTAL)
                .tag("error", errorCode)
                .description("Total redirect errors by error code")
                .register(registry)
                .increment();
    }

    public void incrementAnalyticsFailure() {
        Counter.builder(ANALYTICS_FAILURE_TOTAL)
                .description("Total analytics recording failures")
                .register(registry)
                .increment();
    }

    public Timer.Sample startRedirectTimer() {
        return Timer.start(registry);
    }

    public void stopRedirectTimer(Timer.Sample sample) {
        sample.stop(Timer.builder(REDIRECT_LATENCY)
                .description("Redirect latency in seconds")
                .register(registry));
    }
}
