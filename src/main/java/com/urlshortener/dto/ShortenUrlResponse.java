package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing shortened URL details")
public class ShortenUrlResponse {

    @Schema(description = "Unique identifier for the shortened URL", example = "1")
    private Long id;

    @Schema(description = "The short code", example = "abc123")
    private String shortCode;

    @Schema(description = "The original long URL", example = "https://www.example.com/very/long/url")
    private String originalUrl;

    @Schema(description = "Custom alias for the shortened URL", example = "myalias")
    private String customAlias;

    @Schema(description = "Number of times the URL has been clicked", example = "42")
    private Long clickCount;

    @Schema(description = "Description of the shortened URL", example = "My awesome shortened URL")
    private String description;

    @Schema(description = "Whether the URL is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Timestamp when the URL was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the URL was last updated", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Full shortened URL", example = "http://localhost:8080/api/s/abc123")
    private String shortenedUrl;
}

