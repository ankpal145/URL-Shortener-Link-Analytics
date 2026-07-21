package com.urlshortener.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
        name = "click_event",
        indexes = {
                @Index(name = "idx_click_link_time", columnList = "link_id, clicked_at"),
                @Index(name = "idx_click_link_ip", columnList = "link_id, ip_hash")
        }
)
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_id", nullable = false)
    private Long linkId;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "referer", length = 512)
    private String referer;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    protected ClickEvent() {
    }

    public ClickEvent(Long linkId, Instant clickedAt, String referer, String userAgent, String ipHash) {
        this.linkId = linkId;
        this.clickedAt = clickedAt;
        this.referer = referer;
        this.userAgent = userAgent;
        this.ipHash = ipHash;
    }

    public Long getId() {
        return id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public String getReferer() {
        return referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpHash() {
        return ipHash;
    }
}
