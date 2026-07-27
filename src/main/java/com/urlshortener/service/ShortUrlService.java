package com.urlshortener.service;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.DuplicateAliasException;
import com.urlshortener.repository.ShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ShortUrlService {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private static final String BASE_URL = "http://localhost:8080/api/s/";

    /**
     * Generate a unique short code
     */
    private String generateShortCode() {
        String code;
        do {
            long timestamp = System.currentTimeMillis();
            code = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(Long.toString(timestamp).getBytes())
                    .substring(0, Math.min(10, Base64.getUrlEncoder().withoutPadding()
                            .encodeToString(Long.toString(timestamp).getBytes()).length()));
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }

    /**
     * Create a shortened URL
     */
    public ShortenUrlResponse createShortUrl(ShortenUrlRequest request) {
        log.info("Creating shortened URL for: {}", request.getOriginalUrl());

        String shortCode = generateShortCode();
        String customAlias = (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
                ? request.getCustomAlias().trim()
                : shortCode;

        // Check if custom alias already exists
        if (shortUrlRepository.existsByCustomAlias(customAlias)) {
            log.warn("Custom alias already exists: {}", customAlias);
            throw new DuplicateAliasException("Custom alias '" + customAlias + "' is already in use");
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(request.getOriginalUrl());
        shortUrl.setCustomAlias(customAlias);
        shortUrl.setDescription(request.getDescription());
        shortUrl.setIsActive(true);

        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("Successfully created shortened URL with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Get a short URL by short code or custom alias
     */
    @Transactional(readOnly = true)
    public ShortenUrlResponse getShortUrl(String shortCode) {
        log.info("Fetching short URL with code: {}", shortCode);
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .or(() -> shortUrlRepository.findByCustomAlias(shortCode))
                .orElseThrow(() -> {
                    log.warn("Short URL not found with code or alias: {}", shortCode);
                    return new ResourceNotFoundException("Short URL not found");
                });
        return mapToResponse(shortUrl);
    }

    /**
     * Get original URL and increment click count
     * Supports lookup by both SHORT_CODE and CUSTOM_ALIAS
     */
    public String getOriginalUrl(String shortCode) {
        log.info("Accessing original URL for short code or alias: {}", shortCode);
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .or(() -> shortUrlRepository.findByCustomAlias(shortCode))
                .orElseThrow(() -> {
                    log.warn("Short URL not found with code or alias: {}", shortCode);
                    return new ResourceNotFoundException("Short URL not found");
                });

        if (!shortUrl.getIsActive()) {
            log.warn("Accessing inactive short URL: {}", shortCode);
            throw new ResourceNotFoundException("This shortened URL is no longer active");
        }

        // Increment click count
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        log.info("Incremented click count for shortCode/alias: {} (ID: {})", shortCode, shortUrl.getId());

        return shortUrl.getOriginalUrl();
    }

    /**
     * Update a short URL
     */
    public ShortenUrlResponse updateShortUrl(Long id, ShortenUrlRequest request) {
        log.info("Updating short URL with ID: {}", id);
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Short URL not found with ID: {}", id);
                    return new ResourceNotFoundException("Short URL not found");
                });

        if (request.getOriginalUrl() != null) {
            shortUrl.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getDescription() != null) {
            shortUrl.setDescription(request.getDescription());
        }

        ShortUrl updated = shortUrlRepository.save(shortUrl);
        log.info("Successfully updated short URL with ID: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Delete a short URL
     */
    public void deleteShortUrl(Long id) {
        log.info("Deleting short URL with ID: {}", id);
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Short URL not found with ID: {}", id);
                    return new ResourceNotFoundException("Short URL not found");
                });
        shortUrlRepository.delete(shortUrl);
        log.info("Successfully deleted short URL with ID: {}", id);
    }

    /**
     * Get all short URLs
     */
    @Transactional(readOnly = true)
    public List<ShortenUrlResponse> getAllShortUrls() {
        log.info("Fetching all short URLs");
        return shortUrlRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search short URLs
     */
    @Transactional(readOnly = true)
    public List<ShortenUrlResponse> searchShortUrls(String searchTerm) {
        log.info("Searching short URLs with term: {}", searchTerm);
        return shortUrlRepository.searchByTerms(searchTerm)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get top clicked URLs
     */
    @Transactional(readOnly = true)
    public List<ShortenUrlResponse> getTopClickedUrls(int limit) {
        log.info("Fetching top {} clicked URLs", limit);
        return shortUrlRepository.findTopClicked(limit)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private ShortenUrlResponse mapToResponse(ShortUrl shortUrl) {
        return ShortenUrlResponse.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .customAlias(shortUrl.getCustomAlias())
                .clickCount(shortUrl.getClickCount())
                .description(shortUrl.getDescription())
                .isActive(shortUrl.getIsActive())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .shortenedUrl(BASE_URL + shortUrl.getShortCode())
                .build();
    }
}
