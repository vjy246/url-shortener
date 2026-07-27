package com.urlshortener.exception;

public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String message) {
        super(message);
    }

    public DuplicateAliasException(String message, Throwable cause) {
        super(message, cause);
    }
}

