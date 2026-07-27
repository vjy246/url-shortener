package com.urlshortener.service;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.entity.ShortUrl;
import com.urlshortener.exception.DuplicateAliasException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @InjectMocks
    private ShortUrlService shortUrlService;

    private ShortenUrlRequest request;
    private ShortUrl shortUrl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new ShortenUrlRequest();
        request.setOriginalUrl("https://www.example.com/very/long/url");
        request.setCustomAlias("myalias");
        request.setDescription("Test URL");

        shortUrl = new ShortUrl();
        shortUrl.setId(1L);
        shortUrl.setShortCode("abc123");
        shortUrl.setOriginalUrl("https://www.example.com/very/long/url");
        shortUrl.setCustomAlias("myalias");
        shortUrl.setDescription("Test URL");
        shortUrl.setClickCount(0L);
        shortUrl.setIsActive(true);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateShortUrl_Success() {
        when(shortUrlRepository.existsByCustomAlias(anyString())).thenReturn(false);
        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortenUrlResponse response = shortUrlService.createShortUrl(request);

        assertNotNull(response);
        assertEquals("https://www.example.com/very/long/url", response.getOriginalUrl());
        assertEquals("myalias", response.getCustomAlias());
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    public void testCreateShortUrl_DuplicateAlias() {
        when(shortUrlRepository.existsByCustomAlias("myalias")).thenReturn(true);

        assertThrows(DuplicateAliasException.class, () -> shortUrlService.createShortUrl(request));
    }

    @Test
    public void testGetShortUrl_Success() {
        when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(shortUrl));

        ShortenUrlResponse response = shortUrlService.getShortUrl("abc123");

        assertNotNull(response);
        assertEquals("abc123", response.getShortCode());
        assertEquals("myalias", response.getCustomAlias());
    }

    @Test
    public void testGetShortUrl_NotFound() {
        when(shortUrlRepository.findByShortCode("invalid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.getShortUrl("invalid"));
    }

    @Test
    public void testGetOriginalUrl_Success() {
        when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        String originalUrl = shortUrlService.getOriginalUrl("abc123");

        assertEquals("https://www.example.com/very/long/url", originalUrl);
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    public void testDeleteShortUrl_Success() {
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));

        shortUrlService.deleteShortUrl(1L);

        verify(shortUrlRepository, times(1)).delete(shortUrl);
    }

    @Test
    public void testDeleteShortUrl_NotFound() {
        when(shortUrlRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.deleteShortUrl(999L));
    }
}

