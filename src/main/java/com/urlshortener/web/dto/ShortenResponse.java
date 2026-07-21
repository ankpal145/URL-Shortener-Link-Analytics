package com.urlshortener.web.dto;

import com.urlshortener.domain.Link;
import java.time.Instant;

public record ShortenResponse(
        String code,
        String shortUrl,
        String originalUrl,
        boolean customAlias,
        Instant createdAt
) {
    public static ShortenResponse from(Link link, String shortUrl) {
        return new ShortenResponse(
                link.getCode(),
                shortUrl,
                link.getOriginalUrl(),
                link.isCustomAlias(),
                link.getCreatedAt());
    }
}
