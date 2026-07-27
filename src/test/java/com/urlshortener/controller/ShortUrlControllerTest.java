package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.exception.DuplicateAliasException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
public class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortUrlService shortUrlService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShortenUrlRequest request;
    private ShortenUrlResponse response;

    @BeforeEach
    public void setUp() {
        request = new ShortenUrlRequest();
        request.setOriginalUrl("https://www.example.com/very/long/url");
        request.setCustomAlias("myalias");
        request.setDescription("Test URL");

        response = ShortenUrlResponse.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl("https://www.example.com/very/long/url")
                .customAlias("myalias")
                .clickCount(0L)
                .description("Test URL")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .shortenedUrl("http://localhost:8080/api/s/abc123")
                .build();
    }

    @Test
    public void testCreateShortUrl_Success() throws Exception {
        when(shortUrlService.createShortUrl(any(ShortenUrlRequest.class))).thenReturn(response);

        mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.customAlias").value("myalias"))
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/very/long/url"));
    }

    @Test
    public void testCreateShortUrl_DuplicateAlias() throws Exception {
        when(shortUrlService.createShortUrl(any(ShortenUrlRequest.class)))
                .thenThrow(new DuplicateAliasException("Custom alias 'myalias' is already in use"));

        mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateShortUrl_InvalidRequest() throws Exception {
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest();
        invalidRequest.setOriginalUrl(""); // Invalid: blank URL
        invalidRequest.setCustomAlias("alias");

        mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetShortUrl_Success() throws Exception {
        when(shortUrlService.getShortUrl("abc123")).thenReturn(response);

        mockMvc.perform(get("/urls/abc123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shortCode").value("abc123"));
    }

    @Test
    public void testGetShortUrl_NotFound() throws Exception {
        when(shortUrlService.getShortUrl("invalid"))
                .thenThrow(new ResourceNotFoundException("Short URL not found"));

        mockMvc.perform(get("/urls/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRedirectToOriginal_Success() throws Exception {
        when(shortUrlService.getOriginalUrl("abc123"))
                .thenReturn("https://www.example.com/very/long/url");

        mockMvc.perform(get("/urls/redirect/abc123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example.com/very/long/url"));
    }

    @Test
    public void testRedirectToOriginal_NotFound() throws Exception {
        when(shortUrlService.getOriginalUrl("invalid"))
                .thenThrow(new ResourceNotFoundException("Short URL not found"));

        mockMvc.perform(get("/urls/redirect/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateShortUrl_Success() throws Exception {
        response.setDescription("Updated Description");
        when(shortUrlService.updateShortUrl(eq(1L), any(ShortenUrlRequest.class))).thenReturn(response);

        mockMvc.perform(put("/urls/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    public void testUpdateShortUrl_NotFound() throws Exception {
        when(shortUrlService.updateShortUrl(eq(999L), any(ShortenUrlRequest.class)))
                .thenThrow(new ResourceNotFoundException("Short URL not found"));

        mockMvc.perform(put("/urls/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteShortUrl_Success() throws Exception {
        mockMvc.perform(delete("/urls/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteShortUrl_NotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Short URL not found"))
                .when(shortUrlService).deleteShortUrl(999L);

        mockMvc.perform(delete("/urls/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllShortUrls_Success() throws Exception {
        List<ShortenUrlResponse> responses = Arrays.asList(response);
        when(shortUrlService.getAllShortUrls()).thenReturn(responses);

        mockMvc.perform(get("/urls")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testSearchShortUrls_Success() throws Exception {
        List<ShortenUrlResponse> responses = Arrays.asList(response);
        when(shortUrlService.searchShortUrls("myalias")).thenReturn(responses);

        mockMvc.perform(get("/urls/search?term=myalias")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customAlias").value("myalias"));
    }

    @Test
    public void testGetTopClickedUrls_Success() throws Exception {
        response.setClickCount(150L);
        List<ShortenUrlResponse> responses = Arrays.asList(response);
        when(shortUrlService.getTopClickedUrls(10)).thenReturn(responses);

        mockMvc.perform(get("/urls/analytics/top?limit=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clickCount").value(150));
    }
}

