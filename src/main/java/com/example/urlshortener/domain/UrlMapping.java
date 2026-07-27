package com.example.urlshortener.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "url_mappings")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", unique = true, nullable = false, length = 32)
    private String shortCode;

    @Column(name = "destination_url", nullable = false, length = 2048)
    private String destinationUrl;

    @Column(name = "click_count", nullable = false)
    private long clickCount;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UrlMapping() {
    }

    public UrlMapping(String shortCode, String destinationUrl, Instant expiresAt, Instant createdAt) {
        this.shortCode = shortCode;
        this.destinationUrl = destinationUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.clickCount = 0;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
