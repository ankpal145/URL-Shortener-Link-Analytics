package com.urlshortener.service;

import com.urlshortener.web.error.InvalidUrlException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Validates and normalizes user-supplied URLs.
 *
 * <p>Normalization is intentionally conservative so idempotent lookups behave
 * predictably without silently changing user intent:
 * <ul>
 *   <li>Scheme + host lower-cased</li>
 *   <li>Default ports stripped (80 for http, 443 for https)</li>
 *   <li>A single trailing '/' on an otherwise empty path is stripped</li>
 *   <li>Query &amp; fragment kept verbatim (order-preserving)</li>
 * </ul>
 * We do <em>not</em> touch query-string ordering, percent-encoding, or
 * tracking params - two URLs that differ in those get separate mappings.</p>
 */
@Component
public class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_URL_LENGTH = 2048;

    public String normalize(String rawUrl) {
        if (rawUrl == null) {
            throw new InvalidUrlException("URL must not be null");
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidUrlException("URL must not be blank");
        }
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException("URL exceeds max length of " + MAX_URL_LENGTH);
        }

        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("URL is malformed: " + e.getMessage());
        }
        if (!uri.isAbsolute()) {
            throw new InvalidUrlException("URL must be absolute and include a scheme");
        }
        String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme)) {
            throw new InvalidUrlException("Only http and https URLs are allowed");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidUrlException("URL must include a host");
        }

        String host = uri.getHost().toLowerCase(Locale.ROOT);
        int port = uri.getPort();
        if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
            port = -1;
        }

        String path = uri.getRawPath();
        if (path == null) {
            path = "";
        }
        if (path.equals("/")) {
            path = "";
        } else if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        StringBuilder normalized = new StringBuilder();
        normalized.append(scheme).append("://").append(host);
        if (port != -1) {
            normalized.append(':').append(port);
        }
        normalized.append(path);
        if (uri.getRawQuery() != null) {
            normalized.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            normalized.append('#').append(uri.getRawFragment());
        }
        return normalized.toString();
    }
}
