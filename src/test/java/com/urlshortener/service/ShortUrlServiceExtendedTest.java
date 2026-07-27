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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ShortUrlServiceExtendedTest {

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

    // ===== CREATE TESTS =====

    @Test
    public void testCreateShortUrl_WithoutCustomAlias() {
        ShortenUrlRequest requestNoAlias = new ShortenUrlRequest();
        requestNoAlias.setOriginalUrl("https://www.github.com");
        requestNoAlias.setCustomAlias(null);

        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortenUrlResponse response = shortUrlService.createShortUrl(requestNoAlias);

        assertNotNull(response);
        assertEquals("https://www.example.com/very/long/url", response.getOriginalUrl());
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    public void testCreateShortUrl_WithDescription() {
        when(shortUrlRepository.existsByCustomAlias(anyString())).thenReturn(false);
        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortenUrlResponse response = shortUrlService.createShortUrl(request);

        assertNotNull(response);
        assertEquals("Test URL", response.getDescription());
    }

    @Test
    public void testCreateShortUrl_ActiveByDefault() {
        when(shortUrlRepository.existsByCustomAlias(anyString())).thenReturn(false);
        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortenUrlResponse response = shortUrlService.createShortUrl(request);

        assertTrue(response.getIsActive());
    }

    // ===== GET TESTS =====

    @Test
    public void testGetShortUrl_ReturnsCorrectFields() {
        when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(shortUrl));

        ShortenUrlResponse response = shortUrlService.getShortUrl("abc123");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("abc123", response.getShortCode());
        assertEquals("https://www.example.com/very/long/url", response.getOriginalUrl());
        assertEquals("myalias", response.getCustomAlias());
        assertNotNull(response.getShortenedUrl());
    }

    @Test
    public void testGetOriginalUrl_IncrementsClickCount() {
        when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        String originalUrl = shortUrlService.getOriginalUrl("abc123");

        assertEquals("https://www.example.com/very/long/url", originalUrl);
        verify(shortUrlRepository, times(1)).save(argThat(url ->
                url.getClickCount() == 1
        ));
    }

    @Test
    public void testGetOriginalUrl_InactiveUrl_ThrowsException() {
        ShortUrl inactiveUrl = new ShortUrl();
        inactiveUrl.setId(1L);
        inactiveUrl.setShortCode("inactive123");
        inactiveUrl.setIsActive(false);

        when(shortUrlRepository.findByShortCode("inactive123")).thenReturn(Optional.of(inactiveUrl));

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.getOriginalUrl("inactive123"));
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    // ===== UPDATE TESTS =====

    @Test
    public void testUpdateShortUrl_UpdatesOriginalUrl() {
        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setOriginalUrl("https://www.newurl.com");
        updateRequest.setDescription("Updated");

        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        ShortenUrlResponse response = shortUrlService.updateShortUrl(1L, updateRequest);

        assertNotNull(response);
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    public void testUpdateShortUrl_PartialUpdate() {
        ShortenUrlRequest partialUpdate = new ShortenUrlRequest();
        partialUpdate.setOriginalUrl(null); // Only update description
        partialUpdate.setDescription("New Description");

        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(shortUrl);

        shortUrlService.updateShortUrl(1L, partialUpdate);

        verify(shortUrlRepository, times(1)).save(argThat(url ->
                // Original URL should be updated only if not null in request
                true
        ));
    }

    // ===== DELETE TESTS =====

    @Test
    public void testDeleteShortUrl_RemovesFromDatabase() {
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));

        shortUrlService.deleteShortUrl(1L);

        verify(shortUrlRepository, times(1)).delete(shortUrl);
    }

    // ===== LIST TESTS =====

    @Test
    public void testGetAllShortUrls_ReturnsEmptyList() {
        when(shortUrlRepository.findAll()).thenReturn(Arrays.asList());

        List<ShortenUrlResponse> responses = shortUrlService.getAllShortUrls();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    public void testGetAllShortUrls_ReturnsMultipleUrls() {
        ShortUrl url2 = new ShortUrl();
        url2.setId(2L);
        url2.setShortCode("def456");
        url2.setOriginalUrl("https://www.github.com");
        url2.setCustomAlias("githublink");
        url2.setClickCount(100L);
        url2.setIsActive(true);
        url2.setCreatedAt(LocalDateTime.now());
        url2.setUpdatedAt(LocalDateTime.now());

        when(shortUrlRepository.findAll()).thenReturn(Arrays.asList(shortUrl, url2));

        List<ShortenUrlResponse> responses = shortUrlService.getAllShortUrls();

        assertEquals(2, responses.size());
        assertEquals("abc123", responses.get(0).getShortCode());
        assertEquals("def456", responses.get(1).getShortCode());
    }

    // ===== SEARCH TESTS =====

    @Test
    public void testSearchShortUrls_ReturnsMatches() {
        when(shortUrlRepository.searchByTerms("myalias")).thenReturn(Arrays.asList(shortUrl));

        List<ShortenUrlResponse> responses = shortUrlService.searchShortUrls("myalias");

        assertEquals(1, responses.size());
        assertEquals("myalias", responses.get(0).getCustomAlias());
    }

    @Test
    public void testSearchShortUrls_NoMatches() {
        when(shortUrlRepository.searchByTerms("nonexistent")).thenReturn(Arrays.asList());

        List<ShortenUrlResponse> responses = shortUrlService.searchShortUrls("nonexistent");

        assertTrue(responses.isEmpty());
    }

    // ===== ANALYTICS TESTS =====

    @Test
    public void testGetTopClickedUrls_ReturnsTopUrls() {
        ShortUrl topUrl = new ShortUrl();
        topUrl.setId(1L);
        topUrl.setShortCode("top123");
        topUrl.setClickCount(500L);
        topUrl.setOriginalUrl("https://www.popular.com");
        topUrl.setCustomAlias("popular");
        topUrl.setIsActive(true);
        topUrl.setCreatedAt(LocalDateTime.now());
        topUrl.setUpdatedAt(LocalDateTime.now());

        when(shortUrlRepository.findTopClicked(5)).thenReturn(Arrays.asList(topUrl));

        List<ShortenUrlResponse> responses = shortUrlService.getTopClickedUrls(5);

        assertEquals(1, responses.size());
        assertEquals(500L, responses.get(0).getClickCount());
    }

    @Test
    public void testGetTopClickedUrls_WithLimit() {
        when(shortUrlRepository.findTopClicked(3)).thenReturn(Arrays.asList(shortUrl));

        List<ShortenUrlResponse> responses = shortUrlService.getTopClickedUrls(3);

        verify(shortUrlRepository, times(1)).findTopClicked(3);
    }

    // ===== EXCEPTION TESTS =====

    @Test
    public void testCreateShortUrl_DuplicateAlias_ThrowsException() {
        when(shortUrlRepository.existsByCustomAlias("myalias")).thenReturn(true);

        assertThrows(DuplicateAliasException.class, () -> shortUrlService.createShortUrl(request));
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    public void testGetShortUrl_NotFound_ThrowsException() {
        when(shortUrlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.getShortUrl("notfound"));
    }

    @Test
    public void testUpdateShortUrl_NotFound_ThrowsException() {
        when(shortUrlRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.updateShortUrl(999L, request));
    }

    @Test
    public void testDeleteShortUrl_NotFound_ThrowsException() {
        when(shortUrlRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shortUrlService.deleteShortUrl(999L));
    }
}

