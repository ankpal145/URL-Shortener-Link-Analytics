package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

/**
 * One-way, salted hash for client IPs.
 *
 * <p>Storing raw IPs is unnecessary for the analytics we expose and is a
 * privacy risk. We keep only {@code SHA-256(salt || ip)}. Because the salt is
 * app-wide (not per-record), two clicks from the same IP produce the same
 * hash — enough to count unique visitors — but the reverse mapping to an IP
 * requires guessing the IP space and knowing the salt.</p>
 */
@Component
public class IpHasher {

    private final byte[] salt;

    public IpHasher(AppProperties properties) {
        this.salt = properties.getIpHashSalt().getBytes(StandardCharsets.UTF_8);
    }

    public String hash(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] digest = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
