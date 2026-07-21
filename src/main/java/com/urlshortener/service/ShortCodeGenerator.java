package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Generates URL-safe short codes using {@link SecureRandom} over a Base62 alphabet.
 *
 * <p>With a 7-character code and a 62-character alphabet the code space is
 * 62^7 &asymp; 3.52 * 10^12. Uniqueness is enforced by the database's unique
 * constraint on {@code link.code}; this generator is only responsible for
 * producing candidates. The service layer retries a small number of times on
 * insert-time collisions.</p>
 */
@Component
public class ShortCodeGenerator {

    private static final char[] ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final AppProperties properties;

    public ShortCodeGenerator(AppProperties properties) {
        this.properties = properties;
    }

    public String generate() {
        return generate(properties.getShortCode().getLength());
    }

    public String generate(int length) {
        if (length < 4 || length > 32) {
            throw new IllegalArgumentException("Short code length must be between 4 and 32");
        }
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(out);
    }
}
