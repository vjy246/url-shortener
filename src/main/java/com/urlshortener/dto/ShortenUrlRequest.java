package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a shortened URL")
public class ShortenUrlRequest {

    @NotBlank(message = "Original URL cannot be blank")
    @Schema(description = "The long URL to be shortened", example = "https://www.example.com/very/long/url")
    private String originalUrl;

    @Schema(description = "Custom alias for the shortened URL (optional; leave empty to auto-generate)", example = "")
    private String customAlias;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional description for the shortened URL", example = "My awesome shortened URL")
    private String description;
}
