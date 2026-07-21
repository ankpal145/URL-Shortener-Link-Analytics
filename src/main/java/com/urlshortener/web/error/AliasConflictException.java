package com.urlshortener.web.error;

public class AliasConflictException extends RuntimeException {
    public AliasConflictException(String message) {
        super(message);
    }
}
