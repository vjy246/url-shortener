package com.example.urlshortener.integration;

import com.example.urlshortener.api.CreateUrlRequest;
import com.example.urlshortener.api.CreateUrlResponse;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class UrlShortenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository repository;

    @LocalServerPort
    private int port;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void createAndRedirect() throws Exception {
        // Create a short URL
        CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
        String responseBody = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CreateUrlResponse response = objectMapper.readValue(responseBody, CreateUrlResponse.class);
        assertThat(response.shortCode()).isNotBlank();
        assertThat(response.destinationUrl()).isEqualTo("https://example.com");

        // Redirect should return 302 and increment click count
        mockMvc.perform(get("/" + response.shortCode()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));

        // Analytics should show click count of 1
        String analyticsBody = mockMvc.perform(get("/api/urls/" + response.shortCode() + "/analytics"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(analyticsBody).contains("\"clickCount\":1");
    }

    @Test
    void duplicateCustomAlias_returns409() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest("https://example.com", "my-alias", null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_ALIAS"));
    }

    @Test
    void expiredUrl_returns410() throws Exception {
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.HOURS);

        // Insert an expired mapping directly
        repository.saveAndFlush(new com.example.urlshortener.domain.UrlMapping(
                "expired-code", "https://example.com", pastExpiration, Instant.now().minus(2, ChronoUnit.HOURS)));

        mockMvc.perform(get("/expired-code"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("URL_EXPIRED"));
    }

    @Test
    void unknownShortCode_returns404() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("URL_NOT_FOUND"));
    }

    @Test
    void invalidDestinationUrl_returns400() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest("ftp://not-allowed.com", null, null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void reservedAlias_returns400() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest("https://example.com", "actuator", null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pastExpiration_returns400() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest(
                "https://example.com", null, Instant.now().minus(1, ChronoUnit.HOURS));

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void openApiEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void redirect_incrementsClickCount() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest("https://example.com", "click-test", null);
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Perform two redirects
        mockMvc.perform(get("/click-test")).andExpect(status().isFound());
        mockMvc.perform(get("/click-test")).andExpect(status().isFound());

        String analyticsBody = mockMvc.perform(get("/api/urls/click-test/analytics"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(analyticsBody).contains("\"clickCount\":2");
    }
}
