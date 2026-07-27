package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/urls")
@Tag(name = "URL Shortener API", description = "Endpoints for managing shortened URLs")
@Slf4j
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    /**
     * Create a shortened URL
     */
    @PostMapping
    @Operation(
        summary = "Create a shortened URL",
        description = "Converts a long URL into a shortened URL with optional custom alias and description",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ShortenUrlRequest.class),
                examples = @ExampleObject(
                    name = "Default Request",
                    summary = "Leave customAlias empty or omit it to auto-generate a unique alias",
                    value = "{\"originalUrl\":\"https://www.example.com/very/long/url\",\"description\":\"My awesome shortened URL\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "URL shortened successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - originalUrl is required"),
            @ApiResponse(responseCode = "409", description = "Custom alias already in use")
    })
    public ResponseEntity<ShortenUrlResponse> createShortUrl(
            @Valid @RequestBody ShortenUrlRequest request) {
        log.info("POST /urls - Creating short URL");
        ShortenUrlResponse response = shortUrlService.createShortUrl(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Bulk create shortened URLs (5 records at a time)
     */
    @PostMapping("/bulk")
    @Operation(
        summary = "Bulk create shortened URLs",
        description = "Creates multiple shortened URLs at once (5 sample records). All 5 items are pre-filled by default.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "5 Sample URLs",
                    summary = "Create 5 sample URLs at once",
                    value = "[{\"originalUrl\":\"https://github.com/spring-projects/spring-boot\",\"customAlias\":\"spring-boot-repo\",\"description\":\"Spring Boot Official Repository\"},{\"originalUrl\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\",\"customAlias\":\"youtube-tutorial\",\"description\":\"Popular YouTube Tutorial\"},{\"originalUrl\":\"https://docs.oracle.com/javase/17/docs/api\",\"customAlias\":\"java-17-docs\",\"description\":\"Java 17 API Documentation\"},{\"originalUrl\":\"https://www.python.org/downloads\",\"customAlias\":\"python-downloads\",\"description\":\"Python Official Downloads\"},{\"originalUrl\":\"https://developer.mozilla.org/en-US/docs/Web/JavaScript\",\"customAlias\":\"mdn-javascript\",\"description\":\"MDN JavaScript Documentation\"}]"
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "URLs created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Duplicate alias exists")
    })
    public ResponseEntity<List<ShortenUrlResponse>> bulkCreateShortUrls(
            @Valid @RequestBody List<ShortenUrlRequest> requests) {
        log.info("POST /urls/bulk - Creating {} shortened URLs", requests.size());
        List<ShortenUrlResponse> responses = requests.stream()
                .map(shortUrlService::createShortUrl)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    /**
     * Get short URL details
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Get short URL details", description = "Retrieves details of a shortened URL by its short code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL found"),
            @ApiResponse(responseCode = "404", description = "URL not found")
    })
    public ResponseEntity<ShortenUrlResponse> getShortUrl(
            @Parameter(
                description = "Short code or custom alias of the URL",
                examples = {
                    @ExampleObject(name = "Spring Boot Repo", value = "spring-boot-repo"),
                    @ExampleObject(name = "Java Docs",        value = "java-17-docs"),
                    @ExampleObject(name = "YouTube Tutorial", value = "youtube-tutorial")
                }
            )
            @PathVariable String shortCode) {
        log.info("GET /urls/{} - Fetching short URL details", shortCode);
        ShortenUrlResponse response = shortUrlService.getShortUrl(shortCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Redirect to original URL
     */
    @GetMapping("/redirect/{shortCode}")
    @Operation(
        summary = "Redirect to original URL",
        description = "Redirects to the original URL and increments the click count by 1."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirected successfully"),
            @ApiResponse(responseCode = "404", description = "URL not found or inactive")
    })
    public ResponseEntity<Void> redirectToOriginal(
            @Parameter(
                description = "Short code or custom alias of the URL",
                examples = {
                    @ExampleObject(name = "Spring Boot Repo", value = "spring-boot-repo"),
                    @ExampleObject(name = "Java Docs",        value = "java-17-docs"),
                    @ExampleObject(name = "YouTube Tutorial", value = "youtube-tutorial")
                }
            )
            @PathVariable String shortCode) {
        log.info("GET /urls/redirect/{} - Redirecting to original URL", shortCode);
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }

    /**
     * Update a short URL
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a short URL",
        description = "Updates the original URL and/or description of an existing shortened URL.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Fields to update (all fields are optional)",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ShortenUrlRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Update URL and Description",
                        summary = "Change both the original URL and description",
                        value = """
                            {
                              "originalUrl": "https://github.com/spring-projects/spring-boot/releases",
                              "description": "Spring Boot Releases Page - Updated"
                            }"""
                    ),
                    @ExampleObject(
                        name = "Update Description Only",
                        summary = "Change only the description",
                        value = """
                            {
                              "description": "Updated description text"
                            }"""
                    ),
                    @ExampleObject(
                        name = "Update URL Only",
                        summary = "Change only the original URL",
                        value = """
                            {
                              "originalUrl": "https://www.newurl.com/updated-path"
                            }"""
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL updated successfully"),
            @ApiResponse(responseCode = "404", description = "URL not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ShortenUrlResponse> updateShortUrl(
            @Parameter(description = "ID of the short URL to update", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ShortenUrlRequest request) {
        log.info("PUT /urls/{} - Updating short URL", id);
        ShortenUrlResponse response = shortUrlService.updateShortUrl(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a short URL
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a short URL", description = "Permanently deletes a shortened URL by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "URL not found")
    })
    public ResponseEntity<Void> deleteShortUrl(
            @Parameter(description = "ID of the short URL to delete", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /urls/{} - Deleting short URL", id);
        shortUrlService.deleteShortUrl(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all short URLs
     */
    @GetMapping
    @Operation(summary = "Get all short URLs", description = "Retrieves all shortened URLs in the system")
    @ApiResponse(responseCode = "200", description = "List of URLs retrieved successfully")
    public ResponseEntity<List<ShortenUrlResponse>> getAllShortUrls() {
        log.info("GET /urls - Fetching all short URLs");
        List<ShortenUrlResponse> responses = shortUrlService.getAllShortUrls();
        return ResponseEntity.ok(responses);
    }

    /**
     * Search short URLs
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search short URLs",
        description = "Search for shortened URLs by matching against the original URL, custom alias, or description."
    )
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<List<ShortenUrlResponse>> searchShortUrls(
            @Parameter(
                description = "Search term to match against URL, alias, or description",
                examples = {
                    @ExampleObject(name = "Search Java",    value = "java",         summary = "Find Java-related URLs"),
                    @ExampleObject(name = "Search Python",  value = "python",       summary = "Find Python-related URLs"),
                    @ExampleObject(name = "Search GitHub",  value = "github",       summary = "Find GitHub links"),
                    @ExampleObject(name = "Search Tutorial",value = "tutorial",     summary = "Find tutorial links"),
                    @ExampleObject(name = "Search Guide",   value = "guide",        summary = "Find guide links")
                }
            )
            @RequestParam String term) {
        log.info("GET /urls/search - Searching with term: {}", term);
        List<ShortenUrlResponse> responses = shortUrlService.searchShortUrls(term);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get top clicked URLs
     */
    @GetMapping("/analytics/top")
    @Operation(
        summary = "Get top clicked URLs",
        description = "Retrieves the most-clicked shortened URLs, ordered by click count descending."
    )
    @ApiResponse(responseCode = "200", description = "Top URLs retrieved successfully")
    public ResponseEntity<List<ShortenUrlResponse>> getTopClickedUrls(
            @Parameter(
                description = "Number of top URLs to retrieve",
                examples = {
                    @ExampleObject(name = "Top 5",  value = "5"),
                    @ExampleObject(name = "Top 10", value = "10"),
                    @ExampleObject(name = "Top 20", value = "20")
                }
            )
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /urls/analytics/top - Fetching top {} clicked URLs", limit);
        List<ShortenUrlResponse> responses = shortUrlService.getTopClickedUrls(limit);
        return ResponseEntity.ok(responses);
    }
}
