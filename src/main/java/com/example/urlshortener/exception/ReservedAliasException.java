package com.example.urlshortener.exception;

public class ReservedAliasException extends RuntimeException {

    public ReservedAliasException(String message) {
        super(message);
    }
}
