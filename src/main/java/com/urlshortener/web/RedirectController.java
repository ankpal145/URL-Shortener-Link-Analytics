package com.urlshortener.web;

import com.urlshortener.domain.Link;
import com.urlshortener.service.LinkService;
import com.urlshortener.web.error.LinkNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final LinkService linkService;

    public RedirectController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code, HttpServletRequest request) {
        Link link = linkService.findByCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(link.getOriginalUrl()))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }
}
