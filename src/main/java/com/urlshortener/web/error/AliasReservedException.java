package com.urlshortener.web.error;

public class AliasReservedException extends RuntimeException {
    public AliasReservedException(String message) {
        super(message);
    }
}
