package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.domain.Link;
import com.urlshortener.repository.LinkRepository;
import com.urlshortener.web.error.AliasConflictException;
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
    private final AliasPolicy aliasPolicy;
    private final AppProperties properties;
    private final Clock clock;

    public LinkService(LinkRepository linkRepository,
                       ShortCodeGenerator shortCodeGenerator,
                       UrlValidator urlValidator,
                       AliasPolicy aliasPolicy,
                       AppProperties properties,
                       Clock clock) {
        this.linkRepository = linkRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlValidator = urlValidator;
        this.aliasPolicy = aliasPolicy;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Shorten a URL, optionally under a custom alias.
     *
     * <p>Behaviour:
     * <ul>
     *   <li><b>No alias, URL already shortened:</b> idempotent - returns the existing mapping.</li>
     *   <li><b>No alias, URL is new:</b> generates a random 7-char Base62 code.</li>
     *   <li><b>Alias provided, alias free:</b> creates a new mapping under that alias
     *       (even if the URL is already shortened elsewhere - deliberate: users often
     *       want a branded alias in addition to an auto code).</li>
     *   <li><b>Alias provided, alias taken by a different URL:</b> 409 AliasConflict.</li>
     *   <li><b>Alias provided, alias taken by the same URL:</b> idempotent - returns it.</li>
     *   <li><b>Alias is a reserved word:</b> 400 AliasReserved.</li>
     * </ul>
     */
    @Transactional
    public Link shorten(String rawUrl, String alias) {
        String normalized = urlValidator.normalize(rawUrl);
        String trimmedOriginal = rawUrl.trim();

        if (alias != null && !alias.isBlank()) {
            aliasPolicy.validate(alias);
            Optional<Link> existingForAlias = linkRepository.findByCode(alias);
            if (existingForAlias.isPresent()) {
                Link existing = existingForAlias.get();
                if (existing.getOriginalUrlNormalized().equals(normalized)) {
                    return existing;
                }
                throw new AliasConflictException("Alias '" + alias + "' is already in use");
            }
            Instant now = Instant.now(clock);
            try {
                return linkRepository.saveAndFlush(
                        new Link(alias, trimmedOriginal, normalized, true, now));
            } catch (DataIntegrityViolationException ex) {
                Optional<Link> raced = linkRepository.findByCode(alias);
                if (raced.isPresent() && raced.get().getOriginalUrlNormalized().equals(normalized)) {
                    return raced.get();
                }
                throw new AliasConflictException("Alias '" + alias + "' is already in use");
            }
        }

        Optional<Link> existing = linkRepository.findByOriginalUrlNormalized(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        return persistWithGeneratedCode(trimmedOriginal, normalized);
    }

    @Transactional(readOnly = true)
    public Optional<Link> findByCode(String code) {
        return linkRepository.findByCode(code);
    }

    private Link persistWithGeneratedCode(String originalUrl, String normalized) {
        int maxRetries = properties.getShortCode().getMaxCollisionRetries();
        Instant now = Instant.now(clock);
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            String code = shortCodeGenerator.generate();
            if (aliasPolicy.isReserved(code) || linkRepository.existsByCode(code)) {
                continue;
            }
            try {
                return linkRepository.saveAndFlush(new Link(code, originalUrl, normalized, false, now));
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
