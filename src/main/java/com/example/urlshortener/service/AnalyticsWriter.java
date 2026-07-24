package com.example.urlshortener.service;

import com.example.urlshortener.metrics.UrlMetrics;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnalyticsWriter {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsWriter.class);

    private final UrlMappingRepository repository;
    private final UrlMetrics metrics;

    public AnalyticsWriter(UrlMappingRepository repository, UrlMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordClick(Long mappingId) {
        try {
            repository.incrementClickCount(mappingId);
        } catch (Exception e) {
            log.warn("Failed to record click for mapping id={}: {}", mappingId, e.getMessage());
            metrics.incrementAnalyticsFailure();
        }
    }
}
