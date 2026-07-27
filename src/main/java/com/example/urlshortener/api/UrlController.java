package com.example.urlshortener.api;

import com.example.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "URL Shortener", description = "Create, resolve, and analyse short URLs")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(summary = "Create a short URL")
    @ApiResponse(responseCode = "201", description = "Short URL created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Custom alias already in use")
    @PostMapping("/api/urls")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse create(@RequestBody @Valid CreateUrlRequest request) {
        return urlService.createUrl(request);
    }

    @Operation(summary = "Redirect to the destination URL")
    @ApiResponse(responseCode = "302", description = "Redirect to destination")
    @ApiResponse(responseCode = "404", description = "Short URL not found")
    @ApiResponse(responseCode = "410", description = "Short URL has expired")
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String destination = urlService.resolveRedirect(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destination))
                .build();
    }

    @Operation(summary = "Get analytics for a short URL")
    @ApiResponse(responseCode = "200", description = "Analytics data")
    @ApiResponse(responseCode = "404", description = "Short URL not found")
    @GetMapping("/api/urls/{shortCode}/analytics")
    public AnalyticsResponse analytics(@PathVariable String shortCode) {
        return urlService.getAnalytics(shortCode);
    }
}
