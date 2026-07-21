package com.urlshortener.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "link",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_link_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_link_original_url_normalized", columnNames = "original_url_normalized")
        },
        indexes = {
                @Index(name = "idx_link_created_at", columnList = "created_at")
        }
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "original_url_normalized", nullable = false, length = 2048)
    private String originalUrlNormalized;

    @Column(name = "custom_alias", nullable = false)
    private boolean customAlias;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Link() {
    }

    public Link(String code, String originalUrl, String originalUrlNormalized, boolean customAlias, Instant createdAt) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.originalUrlNormalized = originalUrlNormalized;
        this.customAlias = customAlias;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getOriginalUrlNormalized() {
        return originalUrlNormalized;
    }

    public boolean isCustomAlias() {
        return customAlias;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
