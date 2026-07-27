package com.urlshortener.repository;

import com.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * Find a ShortUrl by its short code
     */
    Optional<ShortUrl> findByShortCode(String shortCode);

    /**
     * Find a ShortUrl by its custom alias
     */
    Optional<ShortUrl> findByCustomAlias(String customAlias);

    /**
     * Check if a short code exists
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Check if a custom alias exists
     */
    boolean existsByCustomAlias(String customAlias);

    /**
     * Find all active short URLs
     */
    List<ShortUrl> findByIsActiveTrue();

    /**
     * Find short URLs by a search term in description or custom alias
     */
    @Query("SELECT s FROM ShortUrl s WHERE LOWER(s.customAlias) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ShortUrl> searchByTerms(@Param("searchTerm") String searchTerm);

    /**
     * Get top clicked URLs
     */
    @Query(value = "SELECT * FROM short_urls ORDER BY click_count DESC LIMIT :limit", nativeQuery = true)
    List<ShortUrl> findTopClicked(@Param("limit") int limit);
}

