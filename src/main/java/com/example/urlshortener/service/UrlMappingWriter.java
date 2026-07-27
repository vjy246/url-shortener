package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.exception.DuplicateAliasException;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UrlMappingWriter {

    private final UrlMappingRepository repository;

    public UrlMappingWriter(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UrlMapping save(UrlMapping mapping, boolean isCustomAlias) {
        try {
            return repository.saveAndFlush(mapping);
        } catch (DataIntegrityViolationException e) {
            if (isCustomAlias) {
                throw new DuplicateAliasException(mapping.getShortCode());
            }
            throw e;
        }
    }
}
