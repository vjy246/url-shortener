package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private String customAlias;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.clickCount = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

