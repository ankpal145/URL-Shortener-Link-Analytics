package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urlshortener.web.error.InvalidUrlException;
import org.junit.jupiter.api.Test;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @Test
    void acceptsPlainHttpUrl() {
        assertThat(validator.normalize("http://example.com")).isEqualTo("http://example.com");
    }

    @Test
    void acceptsHttpsWithPathQueryFragment() {
        assertThat(validator.normalize("https://Example.com:443/a/b?x=1&y=2#top"))
                .isEqualTo("https://example.com/a/b?x=1&y=2#top");
    }

    @Test
    void stripsDefaultPortsAndTrailingSlash() {
        assertThat(validator.normalize("http://example.com:80/")).isEqualTo("http://example.com");
        assertThat(validator.normalize("https://example.com:443/foo/")).isEqualTo("https://example.com/foo");
    }

    @Test
    void keepsNonDefaultPort() {
        assertThat(validator.normalize("http://example.com:8080/x")).isEqualTo("http://example.com:8080/x");
    }

    @Test
    void doesNotReorderQueryParams() {
        assertThat(validator.normalize("https://example.com/foo?b=2&a=1"))
                .isEqualTo("https://example.com/foo?b=2&a=1");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> validator.normalize("  "))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsNonHttpScheme() {
        assertThatThrownBy(() -> validator.normalize("ftp://example.com"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> validator.normalize("javascript:alert(1)"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsRelative() {
        assertThatThrownBy(() -> validator.normalize("/path/only"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsSchemeWithoutHost() {
        assertThatThrownBy(() -> validator.normalize("http://"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsMalformed() {
        assertThatThrownBy(() -> validator.normalize("http://exa mple.com"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void rejectsOversized() {
        String big = "http://example.com/" + "a".repeat(3000);
        assertThatThrownBy(() -> validator.normalize(big))
                .isInstanceOf(InvalidUrlException.class);
    }
}
