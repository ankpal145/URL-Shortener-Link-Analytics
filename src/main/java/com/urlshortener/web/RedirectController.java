package com.urlshortener.web;

import com.urlshortener.domain.Link;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.LinkService;
import com.urlshortener.web.error.LinkNotFoundException;
import com.urlshortener.web.util.ClientInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final LinkService linkService;
    private final AnalyticsService analyticsService;

    public RedirectController(LinkService linkService, AnalyticsService analyticsService) {
        this.linkService = linkService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code, HttpServletRequest request) {
        Link link = linkService.findByCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));
        try {
            analyticsService.recordClick(
                    link,
                    request.getHeader("Referer"),
                    request.getHeader("User-Agent"),
                    ClientInfo.extractIp(request));
        } catch (RuntimeException ex) {
            log.warn("Failed to record click for code {}: {}", code, ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(link.getOriginalUrl()))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }
}
