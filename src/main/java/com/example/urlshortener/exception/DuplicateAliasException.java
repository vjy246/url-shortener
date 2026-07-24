package com.example.urlshortener.exception;

public class DuplicateAliasException extends RuntimeException {

    private final String alias;

    public DuplicateAliasException(String alias) {
        super("Alias already in use: " + alias);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
