package com.urlshortener.web.error;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String code) {
        super("No link exists for code '" + code + "'");
    }
}
