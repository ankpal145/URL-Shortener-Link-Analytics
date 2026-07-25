package com.urlshortener.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String baseUrl = "http://localhost:8080";

    @NotBlank
    private String ipHashSalt = "change-me";

    @NotNull
    private ShortCode shortCode = new ShortCode();

    @NotNull
    private Alias alias = new Alias();

    private Set<String> reservedPaths = Set.of(
            "shorten", "stats", "health", "actuator", "error",
            "assets", "index.html", "robots.txt", "vite.svg", "favicon.svg", "icons.svg");

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getIpHashSalt() {
        return ipHashSalt;
    }

    public void setIpHashSalt(String ipHashSalt) {
        this.ipHashSalt = ipHashSalt;
    }

    public ShortCode getShortCode() {
        return shortCode;
    }

    public void setShortCode(ShortCode shortCode) {
        this.shortCode = shortCode;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public Set<String> getReservedPaths() {
        return reservedPaths;
    }

    public void setReservedPaths(Set<String> reservedPaths) {
        this.reservedPaths = reservedPaths;
    }

    public static class ShortCode {
        private int length = 7;
        private int maxCollisionRetries = 5;

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getMaxCollisionRetries() {
            return maxCollisionRetries;
        }

        public void setMaxCollisionRetries(int maxCollisionRetries) {
            this.maxCollisionRetries = maxCollisionRetries;
        }
    }

    public static class Alias {
        private String pattern = "^[A-Za-z0-9_-]{3,32}$";

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}
