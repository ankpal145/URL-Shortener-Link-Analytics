package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.domain.Link;
import com.urlshortener.repository.LinkRepository;
import com.urlshortener.web.error.ShortCodeGenerationException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlValidator urlValidator;
    private final AppProperties properties;
    private final Clock clock;

    public LinkService(LinkRepository linkRepository,
                       ShortCodeGenerator shortCodeGenerator,
                       UrlValidator urlValidator,
                       AppProperties properties,
                       Clock clock) {
        this.linkRepository = linkRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlValidator = urlValidator;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Shorten a URL with an auto-generated code.
     *
     * <p>Behaviour: <b>idempotent by normalized URL</b>. If the same URL has been
     * shortened before, the existing mapping is returned (same code). Callers who
     * need a distinct code per attempt should use a custom alias.</p>
     */
    @Transactional
    public Link shorten(String rawUrl) {
        String normalized = urlValidator.normalize(rawUrl);
        Optional<Link> existing = linkRepository.findByOriginalUrlNormalized(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        return persistWithRetry(rawUrl.trim(), normalized, false);
    }

    @Transactional(readOnly = true)
    public Optional<Link> findByCode(String code) {
        return linkRepository.findByCode(code);
    }

    private Link persistWithRetry(String originalUrl, String normalized, boolean customAlias) {
        int maxRetries = properties.getShortCode().getMaxCollisionRetries();
        Instant now = Instant.now(clock);
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            String code = shortCodeGenerator.generate();
            if (linkRepository.existsByCode(code)) {
                continue;
            }
            try {
                return linkRepository.saveAndFlush(new Link(code, originalUrl, normalized, customAlias, now));
            } catch (DataIntegrityViolationException ex) {
                Optional<Link> raced = linkRepository.findByOriginalUrlNormalized(normalized);
                if (raced.isPresent()) {
                    return raced.get();
                }
            }
        }
        throw new ShortCodeGenerationException(
                "Could not allocate a unique short code after " + (maxRetries + 1) + " attempts");
    }
}
