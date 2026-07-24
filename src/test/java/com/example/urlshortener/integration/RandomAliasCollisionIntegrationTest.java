package com.example.urlshortener.integration;

import com.example.urlshortener.api.CreateUrlRequest;
import com.example.urlshortener.domain.AliasGenerator;
import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies that when the first generated alias collides with an existing entry,
 * the service retries with a new alias and succeeds.
 */
class RandomAliasCollisionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository repository;

    @MockitoBean
    private AliasGenerator aliasGenerator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void retriesOnCollisionAndSucceeds() throws Exception {
        // Pre-seed the colliding alias
        String collidingAlias = "COLLIDE";
        repository.saveAndFlush(new UrlMapping(collidingAlias, "https://existing.com", null, Instant.now()));

        // Generator returns colliding alias first, then a free alias
        when(aliasGenerator.generate())
                .thenReturn(collidingAlias)
                .thenReturn("FREE123");

        CreateUrlRequest request = new CreateUrlRequest("https://new.com", null, null);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("FREE123"));
    }
}
