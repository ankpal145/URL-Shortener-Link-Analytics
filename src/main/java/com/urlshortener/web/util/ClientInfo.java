package com.urlshortener.web.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientInfo {

    private ClientInfo() { }

    /**
     * Extract the best-guess client IP. Honours {@code X-Forwarded-For} (first entry
     * only) since {@code server.forward-headers-strategy=framework} in
     * application.yml means we're comfortable with the proxy chain.
     */
    public static String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            String first = (comma > 0 ? xff.substring(0, comma) : xff).trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
