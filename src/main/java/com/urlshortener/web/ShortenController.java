package com.urlshortener.web;

import com.urlshortener.config.AppProperties;
import com.urlshortener.domain.Link;
import com.urlshortener.service.LinkService;
import com.urlshortener.web.dto.ShortenRequest;
import com.urlshortener.web.dto.ShortenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortenController {

    private final LinkService linkService;
    private final AppProperties properties;

    public ShortenController(LinkService linkService, AppProperties properties) {
        this.linkService = linkService;
        this.properties = properties;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        Link link = linkService.shorten(request.getUrl(), request.getAlias());
        String shortUrl = buildShortUrl(link.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(ShortenResponse.from(link, shortUrl));
    }

    private String buildShortUrl(String code) {
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + code;
    }
}
