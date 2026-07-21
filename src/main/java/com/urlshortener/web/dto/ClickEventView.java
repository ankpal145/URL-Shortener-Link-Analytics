package com.urlshortener.web.dto;

import com.urlshortener.domain.ClickEvent;
import java.time.Instant;

public record ClickEventView(
        Instant clickedAt,
        String referer,
        String userAgent,
        String ipHash
) {
    public static ClickEventView from(ClickEvent event) {
        return new ClickEventView(
                event.getClickedAt(),
                event.getReferer(),
                event.getUserAgent(),
                event.getIpHash());
    }
}
